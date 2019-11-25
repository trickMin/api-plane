apiVersion: networking.istio.io/v1alpha3
kind: PluginManager
metadata:
  name: ${t_plugin_manager_name}
spec:
<#if t_plugin_manager_workload_labels??>
  workloadLabels:
<#list t_plugin_manager_workload_labels as k,v>
    ${k}: ${v}
</#list>
</#if>
  plugin:
<#list t_plugin_manager_plugins as p>
  -
<@indent count=4>${p}</@indent>
</#list>