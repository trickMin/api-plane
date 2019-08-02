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
#@type=istioScheme
#@version=1.0
#@processBean=RewritePluginProcessor

{
  "redirect": {
    "uri": "${spec.uri}",
    "authority": "${spec.authority}"
  }
}