apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: ${t_gateway_name}
  namespace: ${t_namespace}
  labels:
    api_service: ${t_api_service}
spec:
  selector:
    app: ${t_api_gateway}
  servers:
  - port:
      name: http
      number: 82
      protocol: HTTP
    hosts:
      - "*"