apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: ${destination_rule_name!}
  namespace: ${namespace!}
spec:
  host: ${host}
  subsets:
  <#list gateway_instances as gateway>
  - name: ${api.name}.${gateway}
    trafficPolicy:
      loadBalancer:
        simple: ROUND_ROBIN
  </#list>