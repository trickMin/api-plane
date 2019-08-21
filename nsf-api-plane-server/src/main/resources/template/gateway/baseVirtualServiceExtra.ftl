<#if t_api_connect_timeout??>
timeout: ${t_api_connect_timeout?c}ms
</#if>
<#if t_api_retries??>
retries:
  attempts: ${t_api_retries?c}
</#if>
