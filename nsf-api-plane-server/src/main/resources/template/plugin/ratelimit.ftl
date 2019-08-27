#@type=pluginSchema
#@version=1.0
#@description=频控插件schema
{
  "self_check": "function",
  "fields": {
    "limit_by_list": {
      "alias": "限制标识列表",
      "type": "array",
      "item_schema": {
        "fields": {
          "identifier_extractor": {
            "type": "string",
            "alias": "标识提取策略",
            "help": "用于计算请求标识，以供计数使用;例如，Ip, Header[User-Agent]",
            "order_index": 0
          },
          "pre_condition": {
            "type": "array",
            "alias": "频控前置条件",
            "help": "满足前置条件的请求才会进入频控流程. 左变量为identifier_extractor提取的值",
            "item_schema": {
              "fields": {
                "operator": {
                  "type": "string",
                  "enum": [
                    "≈",
                    "!≈",
                    "=",
                    "!="
                  ]
                },
                "right_value":{
                  "type":"string"
                }
              }
            },
            "order_index": 1
          },
          "enable": {
            "default": true,
            "type": "boolean"
          },
          "minute": {
            "alias": "每分钟请求数",
            "type": "number",
            "order_index": 2
          },
          "month": {
            "alias": "每月请求数",
            "type": "number",
            "order_index": 5
          },
          "hour": {
            "alias": "每小时请求数",
            "type": "number",
            "order_index": 3
          },
          "day": {
            "alias": "每天请求数",
            "type": "number",
            "order_index": 4
          },
          "year": {
            "alias": "每年请求数",
            "type": "number",
            "order_index": 6
          },
          "second": {
            "alias": "每秒请求数",
            "type": "number",
            "order_index": 2
          }
        }
      },
      "help": "1.频率限制是指限制带同一标识请求在给定时间段内可处理的次数;\n2.user-开头的是指用户在某维度的参数, 不以user-开头的是指下游服务器的参数;\n3.使用user-xxx参数的前提是\"请求转换插件\"已经将xxx参数转换为user-xxx.;\n4.当前版本计数方式限制为local, 所以实际频控限制=配置的限制*集群节点数量",
      "order_index": 6
    },
    "policy": {
      "enum": [
        "local",
        "global"
      ],
      "alias": "计数数据存储方式",
      "order_index": 9,
      "default": "redis",
      "type": "string"
    },
    "hide_client_headers": {
      "alias": "隐藏响应中频控信息Header",
      "help": "\"频控信息Header\"包括\"X-RateLimit-Limit-${timeUnit}\"和\"X-RateLimit-Remaining-${timeUnit}\",分别表示在${timeUnit}内已消耗的访问次数和剩余可访问的次数. 例如\"X-RateLimit-Remaining-hour:3\"表示一小时内剩余可访问次数为3",
      "type": "boolean",
      "default": true,
      "order_index": 8
    },
    "refuse_strategy": {
      "help": "频率超额后的处理方式;directly-refuse:直接拒绝访问;rms: 将请求交给rms插件, 由其判定最终结果(rms插件需开启)",
      "enum": [
        "directly-refuse",
        "rms"
      ],
      "alias": "超频后的处理",
      "type": "string",
      "default": "directly-refuse",
      "order_index": 10
    },
    "fault_tolerant": {
      "alias": "差错容忍",
      "help": "当频控插件出现内部错误时(如连不上数据库),\n      若该参数设置为true, 则请求会正常发给上游服务器(相当于频控功能暂时失效);\n      若该参数设置为false, 则会直接返回500, 不会将请求转发到上游服务器",
      "type": "boolean",
      "default": true,
      "order_index": 7
    }
  }
}
---
#@type=demo
{
  "name": "ianus-rate-limiting",
  "config": {
    "limit_by_list": [
      {
        "identifier_extractor": "Header[User-Agent]",
        "pre_condition": [
          {
            "operator":"≈",
            "right_value":"Mozilla/5\\.0.*"
          }
        ],
        "enable": true,
        "second": 1,
        "minute": 2,
        "hour": 3,
        "day": 4,
        "month": 5,
        "year": 6
      }
    ],
    "policy": "global",
    "hide_client_headers": true,
    "refuse_strategy": "directly-refuse",
    "fault_tolerant": true,
  }
}
---
#@type=istioResource
#@version=1.0
#@processor=RateLimitProcessor
{}
