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
<#list t_api_plugins as p>
<@indent count=2>${p}</@indent>
</#list>
  - match:
    - uri:
        regex: ${t_api_request_uris}
      <#if t_api_methods ??>
      method:
        regex: ${t_api_methods}
      </#if>
    route:
    <#list t_virtual_service_destinations as ds>
    - destination:
        host: ${ds.host}
        port:
          number: ${ds.port?c}
        subset: ${t_virtual_service_subset_name}
      weight: ${ds.weight}
    </#list>
    name: ${t_api_name}