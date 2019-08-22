#@type=pluginSchema
#@version=1.0
#@description=黑白名单插件schema
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
    "type": {
      "type": "number",
      "help": "0黑名单，1白名单"
    },
    "ip": {
      "required": true,
      "default": "",
      "type": "array"
    }
  }
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
  "ip_restriction": {
    "type": "${type}",
    "ip": [
<#list ip as item>
  ${item}
</#list>
]
  }
}