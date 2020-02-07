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
    <#if ss.trafficPolicy?has_content>
    trafficPolicy:
      <#if ss.trafficPolicy.loadbalancer?has_content>
      loadBalancer:
        <#if ss.trafficPolicy.loadbalancer.simple?has_content>
        simple: ${ss.trafficPolicy.loadbalancer.simple}
        </#if>
        <#if ss.trafficPolicy.loadbalancer.consistentHash?has_content>
        consistentHash:
          <#if ss.trafficPolicy.loadbalancer.consistentHash.httpHeaderName?has_content>
          httpHeaderName: ${ss.trafficPolicy.loadbalancer.consistentHash.httpHeaderName}
          </#if>
          <#if ss.trafficPolicy.loadbalancer.consistentHash.useSourceIp?has_content>
          useSourceIp: ${ss.trafficPolicy.loadbalancer.consistentHash.useSourceIp}
          </#if>
          <#if ss.trafficPolicy.loadbalancer.consistentHash.httpCookie?has_content>
          httpCookie:
            <#if ss.trafficPolicy.loadbalancer.consistentHash.httpCookie.name?has_content>
            name: ${ss.trafficPolicy.loadbalancer.consistentHash.httpCookie.name}
            </#if>
            <#if ss.trafficPolicy.loadbalancer.consistentHash.httpCookie.ttl?has_content>
            ttl: ${ss.trafficPolicy.loadbalancer.consistentHash.httpCookie.ttl}s
            </#if>
            <#if ss.trafficPolicy.loadbalancer.consistentHash.httpCookie.path?has_content>
            path: ${ss.trafficPolicy.loadbalancer.consistentHash.httpCookie.path}
            </#if>
          </#if>
        </#if>
      </#if>

      <#if ss.trafficPolicy.connectionPool?has_content>
      connectionPool:
        <#if ss.trafficPolicy.connectionPool.tcp?has_content>
        tcp:
          <#if ss.trafficPolicy.connectionPool.tcp.maxConnections?has_content>
          maxConnections: ${ss.trafficPolicy.connectionPool.tcp.maxConnections}
          </#if>
          <#if ss.trafficPolicy.connectionPool.tcp.connectTimeout?has_content>
          connectTimeout: ${ss.trafficPolicy.connectionPool.tcp.connectTimeout}ms
          </#if>
        </#if>
        <#if ss.trafficPolicy.connectionPool.http?has_content>
        http:
          <#if ss.trafficPolicy.connectionPool.http.http1MaxPendingRequests?has_content>
          http1MaxPendingRequests: ${ss.trafficPolicy.connectionPool.http.http1MaxPendingRequests}
          </#if>
          <#if ss.trafficPolicy.connectionPool.http.http2MaxRequests?has_content>
          http2MaxRequests: ${ss.trafficPolicy.connectionPool.http.http2MaxRequests}
          </#if>
          <#if ss.trafficPolicy.connectionPool.http.maxRequestsPerConnection?has_content>
          maxRequestsPerConnection: ${ss.trafficPolicy.connectionPool.http.maxRequestsPerConnection}
          </#if>
          <#if ss.trafficPolicy.connectionPool.http.idleTimeout?has_content>
          idleTimeout: ${ss.trafficPolicy.connectionPool.http.idleTimeout}ms
          </#if>
        </#if>
      </#if>

      <#if ss.trafficPolicy.outlierDetection?has_content>
      outlierDetection:
        <#if ss.trafficPolicy.outlierDetection.consecutiveErrors?has_content>
        consecutiveErrors: ${ss.trafficPolicy.outlierDetection.consecutiveErrors}
        </#if>
        <#if ss.trafficPolicy.outlierDetection.baseEjectionTime?has_content>
        baseEjectionTime: ${ss.trafficPolicy.outlierDetection.baseEjectionTime}ms
        </#if>
        <#if ss.trafficPolicy.outlierDetection.maxEjectionPercent?has_content>
        maxEjectionPercent: ${ss.trafficPolicy.outlierDetection.maxEjectionPercent}
        </#if>
      </#if>
      <#if ss.trafficPolicy.healthCheck?has_content>
      healthCheck:
        <#if t_destination_rule_path?has_content || t_destination_rule_expected_statuses?has_content>
        host: ${t_destination_rule_host}
        </#if>
        <#if ss.trafficPolicy.healthCheck.path?has_content>
        path: ${ss.trafficPolicy.healthCheck.path}
        </#if>
        <#if ss.trafficPolicy.healthCheck.timeout?has_content>
        timeout: ${ss.trafficPolicy.healthCheck.timeout}ms
        </#if>
        <#if ss.trafficPolicy.healthCheck.interval?has_content>
        interval: ${ss.trafficPolicy.healthCheck.interval}ms
        </#if>
        <#if ss.trafficPolicy.healthCheck.healthyThreshold?has_content>
        healthyThreshold: ${ss.trafficPolicy.healthCheck.healthyThreshold}
        </#if>
        <#if ss.trafficPolicy.healthCheck.unhealthyInterval?has_content>
        unhealthyInterval: ${ss.trafficPolicy.healthCheck.unhealthyInterval}ms
        </#if>
        <#if ss.trafficPolicy.healthCheck.unhealthyThreshold?has_content>
        unhealthyThreshold: ${ss.trafficPolicy.healthCheck.unhealthyThreshold}
        </#if>
        <#if ss.trafficPolicy.healthCheck.expectedStatuses?has_content>
        expectedStatuses:
            <#list ss.trafficPolicy.healthCheck.expectedStatuses as s>
            - start: ${s}
            end: ${s+1}
            </#list>
        </#if>
      </#if>
    </#if>
    <#--<@indent count=4><@autoremove><#include "destinationRule_subset_trafficPolicy.ftl"/></@autoremove></@indent>-->
</#list>

</#if>