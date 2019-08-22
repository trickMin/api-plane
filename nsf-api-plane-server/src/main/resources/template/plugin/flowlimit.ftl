#@type=pluginSchema
#@version=1.0
#@description=百分比限流插件schema
{
  "fields": {
    "kind": {
      "type": "string",
      "help": "插件类型"
    },
    "version": {
      "type": "string",
      "help": "插件版本"
    },
    "limit_percent": {
      "type": "number"
    },
    "hash_key": {
      "alias": "hash因子",
      "help": "格式为 source|key1;\n        source|key2 ,其中source为COOKIE、HEADER、QUERY_STRING, UA 。对于UA类型，暂时只支持device-id",
      "required": false,
      "default": "",
      "type": "string"
    }
  },
  "no_consumer": true
}
---
#demo
{
  "version": "${version}",
  "kind": "flowlimit",
  "limit_percent": 50,
  "hash_key" : "source|key1"
}
---
#@type=istioResource
#@version=1.0
#@resourceType=VirtualService
#@fragmentType=NEW_MATCH
{
  "fault": {
    "abort": {
      "percentage": {
        "value": ${limit_percent}
      },
      "httpStatus": 302
    }
  }
}