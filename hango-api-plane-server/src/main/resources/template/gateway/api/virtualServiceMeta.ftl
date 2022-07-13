metadata:
  proxy.filters.http.metadatahub:
<#if t_gateway_name?has_content>
    qz_cluster_name: ${t_gateway_name}
</#if>
<#if t_gateway_ns?has_content>
    qz_cluster_ns: ${t_gateway_ns}
</#if>
<#if t_virtual_service_service_tag?has_content>
    qz_svc_id: ${t_virtual_service_service_tag}
</#if>
<#if t_virtual_service_api_id?has_content>
    qz_api_id: ${t_virtual_service_api_id}
</#if>
<#if t_virtual_service_tenant_id?has_content>
    qz_tenant_id: ${t_virtual_service_tenant_id}
</#if>
<#if t_virtual_service_project_id?has_content>
    qz_project_id: ${t_virtual_service_project_id}
</#if>
<#if t_virtual_service_api_name?has_content>
    qz_api_name: ${t_virtual_service_api_name}
</#if>

<#if t_virtual_service_stats?has_content>
  proxy.metadata_stats.detailed_stats:
    stat_prefix: detailed_route
    stat_tags:
  <#list t_virtual_service_stats?keys as tagKey>
    - key: "${tagKey}"
      val: "${t_virtual_service_stats[tagKey]}"
  </#list>
</#if>