# 白名单用的service account
<#if update?? && update>
apiVersion: v1
kind: ServiceAccount
<#include "../common/metadata.ftl"/>

<#list nsfExtra.targetList! as svc>
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ${svc.name}
  namespace: ${svc.namespace}
</#list>

</#if>