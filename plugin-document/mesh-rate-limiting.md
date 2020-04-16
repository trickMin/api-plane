| 字段                   | 含义         | 范围                  | 备注                   |    |
|:---------------------|:-----------|:--------------------|:---------------------|:---|
| kind                 | 插件类型       | mesh-rate-limiting |                      |    |
| identifier_extractor(可选) | 指定限流header |                     | 格式必须为Header[$header] |    |
| limit_id(可选) | 指定限流生成的id |                     |  |  |
| pre_condition.operator             | 比较符        | ≈，!≈，=，!=，present        |                      |    |
| pre_condition.right_value          | 匹配的value   |                     |                      |    |
| pre_condition.invert(可选)          | 条件反转   | true或false                    |默认为false                      |    |
| type | 限流类型 | 可选项:Local、Global、LocalAvg | |
```
场景1：配置when then
{
  "kind": "mesh-rate-limiting",
  "limit_by_list": [
  {
    "pre_condition": [
    {
      "custom_extractor": "Header[plugin1]",
      "operator": "present",
      "invert": true
    },
    {
      "custom_extractor": "Header[plugin2]",
      "operator": "=",
      "right_value": "ratelimit"
    }
    ],
    "hour": 1,
    "type": "Local",
    "when": "true",
    "then": "@/{pod}"
  }
  ]
}
```
- then中@为占位符，替换unit，例如hour:1 则then: 1/{pod}
- 如果仅配置identifier_extractor没有配置pre_condition则表明是动态根据key的value进行限流
- 限流可以配置多时间维度，支持的维度有:second，hour，minute，day
- identifier_extractor仅支持Header[$header]格式，其他格式会报错