#黑白名单，使用WhiteList进行填充
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRole
metadata:
  name: ${name}
  namespace: ${namespace}
spec:
  rules:
    - services:
        - ${service!}
      constraints:
        - key: "${header!}"
          values:
          <#list values! as val>
          - "${val}"
          </#list>
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRoleBinding
metadata:
  name: ${name}
  namespace: ${namespace}
spec:
  subjects:
<#list users! as user>
    - user: "${user}"
</#list>
  roleRef:
      kind: ServiceRole
      name: ingress

