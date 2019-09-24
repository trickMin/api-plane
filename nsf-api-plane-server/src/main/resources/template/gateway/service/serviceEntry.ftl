apiVersion: networking.istio.io/v1alpha3
kind: ServiceEntry
metadata:
  name: external-svc-https
spec:
  hosts:
  - api.dropboxapi.com
  ports:
  - number: 443
    name: https
    protocol: TLS
  resolution: DNS