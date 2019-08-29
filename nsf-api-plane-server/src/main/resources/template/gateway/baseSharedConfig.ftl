apiVersion: networking.istio.io/v1alpha3
kind: SharedConfig
metadata:
  name: qz-share-config
  namespace: ${t_namespace}
spec:
  rate_limit_configs:
<#list t_shared_config_descriptor as d>
<@indent count=4>${d}</@indent>
</#list>
