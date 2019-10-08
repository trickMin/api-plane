<#if t_virtual_service_hosts?has_content>
hosts:
  <#list t_virtual_service_hosts as host>
  - ${host}
  </#list>
</#if>