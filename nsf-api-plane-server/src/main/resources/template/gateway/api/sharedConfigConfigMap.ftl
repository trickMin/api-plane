apiVersion: v1
kind: ConfigMap
metadata:
  name: ${t_shared_config_name}
  namespace: ${t_shared_config_namespace}
data:
  config:
<#list t_shared_config_descriptor as d>
<@indent count=2>${d}</@indent>
</#list>
