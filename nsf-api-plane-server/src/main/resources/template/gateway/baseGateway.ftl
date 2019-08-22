apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: ${t_gateway_name}
  namespace: ${t_namespace}
spec:
  selector:
    app: ${t_api_gateway}
  servers:
  - port:
      name: http
      number: 83
      protocol: HTTP
    hosts:
      - "*"