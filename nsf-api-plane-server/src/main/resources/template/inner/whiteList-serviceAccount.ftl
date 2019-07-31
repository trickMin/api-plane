# 白名单用的service account
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ${service}
  namespace: ${namespace}

<#list sources! as val>
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ${val}
  namespace: ${sourcesNamespace}
</#list>
