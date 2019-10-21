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
    - "*"
<#if t_api_host_plugins?has_content>
<#list t_api_host_plugins as p>
<@indent count=2>${p}</@indent>
</#list>
</#if>
  http:
<#list t_api_match_plugins as p>
<@indent count=2><@supply>${p}</@supply></@indent>
</#list>
<@indent count=2><@supply></@supply></@indent>

<#if t_api_api_plugins?has_content>
  plugins:
    ${t_api_name}:
<#list t_api_api_plugins as p>
<@indent count=6>${p}</@indent>
</#list>
</#if>