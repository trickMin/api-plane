---
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: ${resource_name!}
  namespace: ${namespace!}
spec:
  selector:
    app: ${gateway_instance}
  servers:
  - port:
      name: http
      number: 80
      protocol: HTTP
    hosts:
      <#list api.hosts as host>
      - ${host}
      </#list>
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: ${resource_name!}
  namespace: ${namespace!}
spec:
  gateways:
  - ${resource_name!}
  hosts:
  <#list api.hosts as host>
  - ${host}
  </#list>
  http:
  - match:
    - uri:
        regex: ${api.requestUris?join("|")}
      method:
        regex: ${api.methods?join("|")}
    route:
    <#list api.proxyUris as proxy>
    <#assign s = api.proxyUris?size>
    - destination:
        host: ${proxy}
        port:
          number: 8080
        subset: ${api.name!}-${gateway_instance}
      weight: <#if proxy_has_next>${(100/s)?int}<#else>${100-(100*(s-1)/s)?int}</#if>
    </#list>
    name: ${api.name}