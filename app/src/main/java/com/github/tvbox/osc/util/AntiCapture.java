package com.github.tvbox.osc.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Process;

import com.github.tvbox.osc.BuildConfig;

import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Release-only runtime integrity checks for the OEM distribution.
 * These checks are deliberately skipped in debug builds so local development
 * remains usable. A release build must receive RELEASE_CERT_SHA256 at build time.
 */
public final class AntiCapture {
    private static final String EXPECTED_PACKAGE = "com.yyds.cn";
    private static final String EXPECTED_LABEL = "云TV盒子";
    private static final String[] INJECTION_CLASS_MARKERS = {
            "bin.mt.plus.Hook",
            "bin.mt.plus.MTApplication",
            "com.np.manager.NPApplication"
    };

    private AntiCapture() {
    }

    public static void assertSecure(Context context) {
        if (BuildConfig.DEBUG) return;

        boolean insecure = !EXPECTED_PACKAGE.equals(context.getPackageName())
                || !EXPECTED_LABEL.equals(readAppLabel(context))
                || !hasExpectedSigningCertificate(context)
                || hasHttpProxy()
                || hasVpnInterface()
                || hasInjectedManagerClass();
        if (insecure) terminate();
    }

    private static String readAppLabel(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        return String.valueOf(context.getPackageManager().getApplicationLabel(info));
    }

    private static boolean hasExpectedSigningCertificate(Context context) {
        String expected = BuildConfig.RELEASE_CERT_SHA256;
        if (expected == null || expected.trim().isEmpty()) return false;
        try {
            PackageManager manager = context.getPackageManager();
            Signature[] signatures;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo info = manager.getPackageInfo(
                        context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                signatures = info.signingInfo.hasMultipleSigners()
                        ? info.signingInfo.getApkContentsSigners()
                        : info.signingInfo.getSigningCertificateHistory();
            } else {
                PackageInfo info = manager.getPackageInfo(
                        context.getPackageName(), PackageManager.GET_SIGNATURES);
                signatures = info.signatures;
            }
            for (Signature signature : signatures) {
                if (expected.equalsIgnoreCase(toSha256(signature.toByteArray()))) return true;
            }
        } catch (Throwable ignored) {
            // A missing or unreadable certificate is treated as an integrity failure.
        }
        return false;
    }

    private static String toSha256(byte[] value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
        StringBuilder result = new StringBuilder();
        for (byte item : digest) {
            if (result.length() > 0) result.append(':');
            result.append(String.format("%02X", item));
        }
        return result.toString();
    }

    private static boolean hasHttpProxy() {
        String host = System.getProperty("http.proxyHost");
        String port = System.getProperty("http.proxyPort");
        return host != null && !host.trim().isEmpty() && port != null && !port.trim().isEmpty();
    }

    private static boolean hasVpnInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) return false;
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                if (!networkInterface.isUp()) continue;
                String name = networkInterface.getName().toLowerCase();
                if (name.startsWith("tun") || name.startsWith("ppp") || name.startsWith("pptp")
                        || name.startsWith("ipsec")) return true;
            }
        } catch (Throwable ignored) {
            // Network interface introspection is best-effort only.
        }
        return false;
    }

    private static boolean hasInjectedManagerClass() {
        for (String className : INJECTION_CLASS_MARKERS) {
            try {
                Class.forName(className, false, AntiCapture.class.getClassLoader());
                return true;
            } catch (ClassNotFoundException ignored) {
                // Expected when the relevant manager is not injected.
            } catch (Throwable ignored) {
                // Continue checking the remaining marker classes.
            }
        }
        return false;
    }

    private static void terminate() {
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}
