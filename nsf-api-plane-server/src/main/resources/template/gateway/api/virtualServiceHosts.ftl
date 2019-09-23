<#if t_virtual_service_hosts??>
hosts:
  <#list t_virtual_service_hosts as host>
  - ${host}
  </#list>
</#if>