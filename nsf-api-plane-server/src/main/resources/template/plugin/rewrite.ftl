---
#@type=pluginTemplate
#@version=1.0
#@description=这是个插件模板

{
  "apiVersion": "${apiVersion}",
  "kind": "RewritePlugin",
  "spec": {
    "uri": "${uri}",
    "authority": "${authority}"
  }
}
---
#@type=istioScheme
#@version=1.0

{
  "redirect": {
    "uri": "${spec.uri}",
    "authority": "${spec.authority}"
  }
}