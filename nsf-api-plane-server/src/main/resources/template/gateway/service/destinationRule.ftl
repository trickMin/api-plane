apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: ${t_destination_rule_name}
spec:
  host: ${t_destination_rule_host}
  altStatName: ${t_destination_rule_alt_stat_name}
<@indent count=2><@autoremove><#include "destinationRule_trafficPolicy.ftl"/></@autoremove></@indent>
  subsets:
  - name: ${t_api_service}-${t_api_gateway}