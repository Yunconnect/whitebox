package com.github.tvbox.osc.util;

import android.content.Context;

/**
 * Runtime guard retained only as a compatibility entry point.
 *
 * <p>The OEM cloud build deliberately produces raw, unsigned APKs without a
 * release-certificate fingerprint or runtime anti-tamper restrictions. Keeping
 * this method as a no-op prevents callers from depending on a particular
 * certificate, package name, network environment, or manager application.</p>
 */
public final class AntiCapture {

    private AntiCapture() {
    }

    public static void assertSecure(Context context) {
        // Raw unsigned distribution: no runtime integrity or anti-tamper check.
    }
}
