#@type=pluginSchema
#@version=1.0
#@description=lua插件schema
{
}
---
#demo
  {
    "version": "1.0",
    "kind": "lua",
    "level": "host",
    "name": "ipRestriction",
    "config": {
      "fields": {
        "block_mesg": {
          "string_value": "RouteLevel: Sorry! your ip be blocked!"
        },
        "blacklist": {
          "list_value": [
            {
              "string_value": "127.0.0.1/32"
            },
            {
              "string_value": "192.168.0.0/16"
            }
          ]
        }
      }
    }
  }
---
#@type=istioResource
#@version=1.0
#@processor=LuaProcessor
{}