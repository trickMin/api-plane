trafficPolicy:
  outlierDetection:
<#if t_destination_rule_consecutive_errors?has_content>
    consecutiveErrors: ${t_destination_rule_consecutive_errors}
</#if>
<#if t_destination_rule_base_ejection_time?has_content>
    baseEjectionTime: ${t_destination_rule_base_ejection_time}ms
</#if>
<#if t_destination_rule_max_ejection_percent?has_content>
    maxEjectionPercent: ${t_destination_rule_max_ejection_percent}
</#if>
  healthCheck:
<#if t_destination_rule_path?has_content>
    path: ${t_destination_rule_path}
</#if>
<#if t_destination_rule_timeout?has_content>
    timeout: ${t_destination_rule_timeout}ms
</#if>
<#if t_destination_rule_healthy_interval?has_content>
    interval: ${t_destination_rule_healthy_interval}ms
</#if>
<#if t_destination_rule_healthy_threshold?has_content>
    healthyThreshold: ${t_destination_rule_healthy_threshold}
</#if>
<#if t_destination_rule_unhealthy_interval?has_content>
    unhealthyInterval: ${t_destination_rule_unhealthy_interval}ms
</#if>
<#if t_destination_rule_unhealthy_threshold?has_content>
    unhealthyThreshold: ${t_destination_rule_unhealthy_threshold}
</#if>
<#if t_destination_rule_expected_statuses?has_content>
    expectedStatuses:
    <#list t_destination_rule_expected_statuses as s>
    - start: ${s}
      end: ${s}
    </#list>
</#if>

