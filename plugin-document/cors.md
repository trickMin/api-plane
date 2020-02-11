| 字段   | 含义   | 范围   | 备注 |    |
|:-----|:-----|:-----|:---|:---|
| kind | 插件类型 | cors |    |    |

```
{
  "kind": "cors",
  "corsPolicy": {
    "allowOrigin": ["www.baidu.com", "google.com"],
    "allowOriginRegex": ["a.*","b.*"],
    "allowMethods": ["get","post"],
    "allowHeaders": [":authority",":method"],
    "exposeHeaders": ["host","user-agent"],
    "maxAge": "30s",
    "allowCredentials": true
  }
}
```