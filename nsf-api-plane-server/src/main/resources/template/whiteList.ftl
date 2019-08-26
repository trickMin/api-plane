# 严选白名单+分流, 使用com.netease.cloud.nsf.meta.WhiteList进行填充
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: ${service}
  namespace: ${namespace}
spec:
  hosts:
  - ${service}
  http:
  - route:
    - destination:
        host: ${service}
        subset: internal
      weight: ${100 - outWeight!0}
    - destination:
        host: qz-egress.istio-system.svc.cluster.local
      weight: ${outWeight!0}
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: ${service}
  namespace: ${namespace}
spec:
  host: ${service}
  trafficPolicy:
    tls:
      mode: ISTIO_MUTUAL
  host: ${service}
  subsets:
  - name: internal
    labels:
      app: ${service}
---
# 白名单配置
apiVersion: "rbac.istio.io/v1alpha1"
kind: ServiceRole
metadata:
  name: ${service}-whitelist
  namespace: ${namespace}
spec:
  rules:
  - services: ["${service}.${namespace}.svc.cluster.local"]
<#--    paths:-->
<#--<#list configAuthPaths! as path>-->
<#--    - ${path}-->
<#--</#list>-->
---
apiVersion: "rbac.istio.io/v1alpha1"
kind: ServiceRoleBinding
metadata:
  name: ${service}-whitelist
  namespace: ${namespace}
spec:
  subjects:
<#list verboseSources! as val>
  - user: cluster.local/ns/${val.namespace}/sa/${val.name}
</#list>
  roleRef:
    kind: ServiceRole
    name: ${service}-whitelist
---
# 白名单配置 - 放行的路径
apiVersion: "rbac.istio.io/v1alpha1"
kind: ServiceRole
metadata:
  name: ${service}-passed
  namespace: ${namespace}
spec:
  rules:
  - services: ["${service}.${namespace}.svc.cluster.local"]
    paths:
<#list configPassedPaths! as path>
    - ${path}
</#list>
---
apiVersion: "rbac.istio.io/v1alpha1"
kind: ServiceRoleBinding
metadata:
  name: ${service}-passed
  namespace: ${namespace}
spec:
  subjects:
  - user: "*"
  roleRef:
    kind: ServiceRole
    name: ${service}-passed
---
apiVersion: "authentication.istio.io/v1alpha1"
kind: "Policy"
metadata:
  name: ${service}
  namespace: ${namespace}
spec:
  targets:
  - name: ${service}
  peers:
  - mtls:
      mode: STRICT
