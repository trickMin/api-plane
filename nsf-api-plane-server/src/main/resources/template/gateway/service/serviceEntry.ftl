apiVersion: networking.istio.io/v1alpha3
kind: ServiceEntry
metadata:
  name: ${t_service_entry_name}
spec:
  endpoints:
<#list endpoints as e>
  - address: ${e.address}
<#if e.port ??>
    ports:
      http: ${e.port?c}
</#if>
</#list>
  exportTo:
  - .
  hosts:
  - ${t_service_entry_host}
  location: MESH_EXTERNAL
  ports:
  - name: http
    number: 80
    protocol: HTTP
  resolution: DNS