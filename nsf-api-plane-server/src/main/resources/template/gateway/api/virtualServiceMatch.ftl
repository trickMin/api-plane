match:
- uri:
    regex: ${t_api_request_uris}
<#if t_api_methods?has_content>
  method:
    regex: ${t_api_methods}
</#if>
<#if t_virtual_service_hosts??>
  headers:
    :authority:
      regex: ${t_virtual_service_hosts}
</#if>
<#if t_api_headers?has_content>
  <#list t_api_headers as h>
    ${h.key}:
      ${h.type}: ${h.value}
  </#list>
</#if>
<#if t_api_query_params?has_content>
  queryParams:
<#list t_api_query_params as p>
    ${p.key}:
      ${p.type}: ${p.value}
</#list>
</#if>
<#if t_virtual_service_match_priority??>
priority: ${t_virtual_service_match_priority}
</#if>