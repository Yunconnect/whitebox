# 云TV盒子 Python 插件

`csp_changzhang_yuntv.py` 已适配云TV盒子的 Python 32 位和 Python 64 位加载器。配置中的 `api` 必须指向以 `.py` 结尾的可访问脚本地址；应用会自动选择 Python 爬虫加载器。

```json
{
  "key": "py_changzhang",
  "name": "厂长资源",
  "type": 3,
  "api": "https://raw.githubusercontent.com/Yunconnect/whitebox/feat/config-ui-no-default-api/plugins/csp_changzhang_yuntv.py",
  "ext": "",
  "searchable": 1,
  "quickSearch": 1,
  "filterable": 1
}
```

如果站点更换域名，可将 `ext` 设置为完整 HTTP 或 HTTPS 地址，例如 `"ext": "https://example.com"`。不要将 `api` 填为网页地址；它必须是直接返回 `.py` 源码的链接。

该插件不需要本地代理；为兼容应用接口，未使用本地代理时会返回标准空响应。
