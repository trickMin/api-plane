字段 | 含义 | 范围 | 备注 |
---|---|---|---|
kind | 插件类型 | static-downgrade | | 
condition | 降级生效的条件 | | |
condition.code | 降级生效的返回状态码 | | |
response | 降级返回 | | |
header | 降级返回header | | 存在则覆盖，不存在则增加 |
body | 降级返回body | | |
```
{
  "kind":"static-downgrade",
  "condition":{
    "code":{
      "regex":"(2..)"
    }
  },
  "response":{
    "code":200,
    "header":{
      "Content-Type":"application/json"
    },
    "body":"{}"
  }
}
```
转换后crd
```
{
  "downgrade_rpx": {
    "status": "(2..)"
  },
  "static_response": {
    "http_status": 299,
    "headers": [
    {
      "key": "buhao",
      "value": "buhao"
    },
    {
      "key": "buhao",
      "value": "buhao"
    }
    ],
    "body": {
      "inline_string": "hohohohohoh"
    }
  }
}
```

- condition降级的条件，目前仅支持code，既返回的状态码，匹配条件也仅支持regex
- response降级的返回，code会返回状态码，header为返回附带的header,body既返回的body