<#if t_virtual_service_destinations??>
route:
<#list t_virtual_service_destinations as ds>
- destination:
    host: ${ds.host}
    port:
      number: ${ds.port?c}
    subset: ${ds.subset}
  weight: ${ds.weight}
</#list>
</#if>