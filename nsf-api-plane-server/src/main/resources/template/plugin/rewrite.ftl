---
#@type=pluginTemplate
#@version=1.0
#@description=这是个插件模板

{
  "version": "${version}",
  "kind": "rewrite",
  "spec": {
    "uri": "${uri}",
    "authority": "${authority}"
  }
}
---
#@type=istioSchema
#@version=1.0

{
  "redirect": {
    "uri": "${spec.uri}",
    "authority": "${spec.authority}"
  }
}