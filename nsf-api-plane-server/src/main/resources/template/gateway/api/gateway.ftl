apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: ${t_gateway_name}
spec:
<#if t_gateway_http_10?has_content && t_gateway_http_10>
  enableHttp10: true
</#if>
  selector:
    gw_cluster: ${t_api_gateway}
  servers:
  - port:
      name: http
      number: 80
      protocol: HTTP
    hosts:
      - "*"