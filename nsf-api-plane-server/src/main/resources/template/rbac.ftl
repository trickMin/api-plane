#黑白名单，使用com.netease.cloud.nsf.meta.WhiteList进行填充
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRole
metadata:
  name: qz-ingress
  namespace: ${namespace}
spec:
  rules:
    - services:
        - ${service}.${namespace}.svc.cluster.local
      constraints:
        - key: "request.headers[Source-External]"
          values:
          <#list sources! as val>
          - "${val}"
          </#list>
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRoleBinding
metadata:
  name: qz-ingress
  namespace: ${namespace}
spec:
  subjects:
    - user: "cluster.local/ns/${namespace}/sa/istio-ingressgateway-service-account"
  roleRef:
      kind: ServiceRole
      name: qz-ingress

