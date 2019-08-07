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
    <#list destinations as ds>
    - destination:
        host: ${ds.host}
        port:
          number: ${ds.port}
        subset: ${api.name!}-${gateway_instance}
      weight: ${ds.weight}
    </#list>
    name: ${api.name}