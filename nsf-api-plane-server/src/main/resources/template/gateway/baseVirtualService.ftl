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
  http:
<#list t_api_match_plugins as p>
<@indent count=2><@supply>${p}</@supply></@indent>
</#list>
<@indent count=2><@supply></@supply></@indent>