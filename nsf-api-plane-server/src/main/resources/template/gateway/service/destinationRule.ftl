apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: ${t_destination_rule_name}
spec:
  host: ${t_destination_rule_host}
  altStatName: ${t_destination_rule_alt_stat_name}
<#--- 默认所有subset都继承同一份trafficPolicy --->
<@indent count=2><@autoremove><#include "destinationRule_trafficPolicy.ftl"/></@autoremove></@indent>
  subsets:
<#--- 默认生成的subset --->
  - name: ${t_api_service}-${t_api_gateway}
    altStatName: ${t_destination_rule_alt_stat_name}
<@indent count=4><@autoremove><#include "destinationRule_trafficPolicy.ftl"/></@autoremove></@indent>
<#--- 自定义的subset --->
<#if t_destination_rule_extra_subsets?has_content>
<#list t_destination_rule_extra_subsets as ss>
  - name: ${ss.name}
    altStatName: ${t_destination_rule_alt_stat_name}
<#if ss.labels?has_content>
    labels:
<#list ss.labels?keys as k>
      ${k}: ${ss.labels[k]}
</#list>
</#if>
<@indent count=4><@autoremove><#include "destinationRule_trafficPolicy.ftl"/></@autoremove></@indent>
</#list>

</#if>