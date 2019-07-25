---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRole
metadata:
  name: ${metadata.name}
  namespace: ${metadata.namespace}
spec:
  rules:
<#list nsfExtra.whiteList.services!?keys as key>
    - services:
        - ${key}
      constraints:
        - key: "${nsfExtra.whiteList.header}"
          values:
          <#list nsfExtra.whiteList.services[key] as val>
          - "${val}"
          </#list>
</#list>
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRoleBinding
metadata:
  name: ${metadata.name}
  namespace: ${metadata.namespace}
spec:
  subjects:
<#list nsfExtra.whiteList.users! as user>
    - user: "${user}"
</#list>
    roleRef:
        kind: ServiceRole
        name: ingress

