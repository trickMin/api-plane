match:
- uri:
    regex: ${t_api_request_uris}
<#if t_api_methods ??>
  method:
    regex: ${t_api_methods}
</#if>
<#if t_virtual_service_hosts??>
  headers:
    :authority:
      regex: ${t_virtual_service_hosts}
</#if>