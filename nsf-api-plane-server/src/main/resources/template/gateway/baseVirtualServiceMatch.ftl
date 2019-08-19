match:
- uri:
    regex: ${t_api_request_uris}
<#if t_api_methods ??>
  method:
    regex: ${t_api_methods}
</#if>