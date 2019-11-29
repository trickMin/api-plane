apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: ${t_destination_rule_name}
spec:
  host: ${t_destination_rule_host}
  altStatName: ${t_destination_rule_alt_stat_name}
<@autoremove><@indent count=2><#include "destinationRule_trafficPolicy.ftl"/></@indent></@autoremove>
  subsets:
  - name: ${t_api_service}-${t_api_gateway}