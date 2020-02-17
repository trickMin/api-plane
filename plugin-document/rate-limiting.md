| 字段                   | 含义         | 范围                  | 备注                   |    |
|:---------------------|:-----------|:--------------------|:---------------------|:---|
| kind                 | 插件类型       | ianus-rate-limiting |                      |    |
| identifier_extractor | 指定限流header |                     | 格式必须为Header[$header] |    |
| operator             | 比较符        | ≈，!≈，=，!=           |                      |    |
| right_value          | 匹配的value   |                     |                      |    |
```
场景1：不配置条件，对整个api限流
{
  "kind": "ianus-rate-limiting",
  "limit_by_list": [
  {
    "hour": 1
  }
  ]
}
```

```
场景2：对特定key-value组合条件限流，例如对plugin=ratelimit请求限流
{
  "kind": "ianus-rate-limiting",
  "limit_by_list": [
  {
    "identifier_extractor": "Header[plugin]",
    "pre_condition": [
    {
      "operator": "=",
      "right_value": "ratelimit"
    }
    ],
    "hour": 1
  }
  ]
}
```

```
场景3：根据key的value动态限流，例如根据xff对每个ip限流
{
  "kind": "ianus-rate-limiting",
  "limit_by_list": [
  {
    "identifier_extractor": "Header[XFF]",
    "hour": 1
  }
  ]
}
```

```
场景4：多维度限流，同时配置多个维度的阈值，例如每秒5个请求，每小时10个
{
  "kind": "ianus-rate-limiting",
  "limit_by_list": [
  {
    "identifier_extractor": "Header[plugin]",
    "pre_condition": [
    {
      "operator": "=",
      "right_value": "ratelimit"
    }
    ],
    "second": 5,
    "hour": 10
  }
  ]
}
```

- 如果仅配置identifier_extractor没有配置pre_condition则表明是动态根据key的value进行限流
- 限流可以配置多时间维度，支持的维度有:second，hour，minute，day
- identifier_extractor仅支持Header[$header]格式，其他格式会报错