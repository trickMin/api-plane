# 严选白名单+分流, 使用com.netease.cloud.nsf.meta.WhiteList进行填充
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: ${service}-vs
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
        host: qz-egress.qz.svc.cluster.local
      weight: ${outWeight!0}
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: ${service}-dst
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
  name: ${service}-svcrole
  namespace: ${namespace}
spec:
  rules:
  - services: ["${service}.${namespace}.svc.cluster.local"]
---
apiVersion: "rbac.istio.io/v1alpha1"
kind: ServiceRoleBinding
metadata:
  name: ${service}-svcrolebinding
  namespace: ${namespace}
spec:
  subjects:
    - user: cluster.local/ns/${namespace}/sa/${service}
  roleRef:
    kind: ServiceRole
    name: ${service}-svcrole
---
apiVersion: "authentication.istio.io/v1alpha1"
kind: "Policy"
metadata:
  name: ${service}-policy
  namespace: ${namespace}
spec:
  targets:
  - name: ${service}
  peers:
  - mtls:
      mode: STRICT
---
# service account
# <#include "inner/whiteList-serviceAccount.ftl"/>