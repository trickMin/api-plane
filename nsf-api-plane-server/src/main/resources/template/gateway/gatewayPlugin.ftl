apiVersion: networking.istio.io/v1alpha3
kind: GatewayPlugin
metadata:
  name: ${t_gateway_plugin_name}
spec:
<#if t_gateway_plugin_gateways?has_content>
  gateway:
  <#list t_gateway_plugin_gateways as g>
  - ${g}
  </#list>
</#if>
<#if t_gateway_plugin_hosts?has_content>
  host:
<#list t_gateway_plugin_hosts as h>
  - ${h}
</#list>
</#if>
<#if t_gateway_plugin_routes?has_content>
  route:
<#list t_gateway_plugin_routes as r>
  - ${r}
</#list>
</#if>
<#if t_gateway_plugin_users?has_content>
  user:
<#list t_gateway_plugin_users as u>
  - ${u}
</#list>
</#if>
<#if t_gateway_plugin_plugins?has_content>
  plugins:
    <#list t_gateway_plugin_plugins as p>
    - name: test
      settings:
<@indent count=8>${p}</@indent>
    </#list>
</#if>