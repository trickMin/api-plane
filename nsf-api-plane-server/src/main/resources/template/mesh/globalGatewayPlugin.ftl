<#-- 网格插件 -->
apiVersion: networking.istio.io/v1alpha3
kind: GatewayPlugin
metadata:
  name: ${t_gateway_plugin_name}
spec:
  gateway:
    - mesh
<#if t_gateway_plugin_hosts?has_content>
  host:
  <#list t_gateway_plugin_hosts as h>
    - ${h}
  </#list>
</#if>
<#if t_gateway_plugin_plugins??>
  plugins:
  <#list t_gateway_plugin_plugins as p>
    -
    <@indent count=6>${p}</@indent>
  </#list>
</#if>