apiVersion: networking.istio.io/v1alpha3
kind: PluginManager
metadata:
  name: ${t_plugin_manager_name}
  namespace: ${t_plugin_manager_namespace}
spec:
<#if t_plugin_manager_workload_labels??>
  workloadLabels:
<#list t_plugin_manager_workload_labels as k,v>
    ${k}: ${v}
</#list>
</#if>
  plugins:
<#list t_plugin_manager_plugins as p>
  - ${p}
</#list>