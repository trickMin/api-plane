apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: ${t_virtual_service_name}
  namespace: ${t_namespace}
  labels:
    api_service: ${t_api_service}
spec:
  gateways:
  - ${t_gateway_name}
  hosts:
  <#list t_virtual_service_hosts as host>
  - ${host}
  </#list>
  http:
<#list t_api_match_plugins as p>
<@indent count=2>${p}</@indent>
</#list>
  - name: ${t_api_name}
<@indent count=4>${t_virtual_service_match}</@indent>
<@indent count=4>${t_virtual_service_route}</@indent>
<@indent count=4>${t_virtual_service_extra}</@indent>