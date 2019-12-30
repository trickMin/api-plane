meta:
  qz_cluster_name: ${t_gateway_name}
<#if t_virtual_service_service_tag?has_content>
  qz_svc_id: ${t_virtual_service_service_tag}
</#if>
<#if t_virtual_service_api_id?has_content>
  qz_api_id: ${t_virtual_service_api_id}
</#if>
<#if t_virtual_service_api_name?has_content>
  qz_api_name: ${t_virtual_service_api_name}
</#if>
