# 严选白名单+分流
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
<#include "common/metadata.ftl"/>
spec:
  hosts:
  - yx-provider
  http:
  - route:
    - destination:
        host: ${metadata.name}
        subset: internal
      weight: ${100 - nsfExtra.outWeight!0}
    - destination:
        host: qz-egress.qz.svc.cluster.local
      weight: ${nsfExtra.outWeight!0}
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
<#include "common/metadata.ftl"/>
spec:
  host: ${metadata.name}
  trafficPolicy:
    tls:
      mode: ISTIO_MUTUAL
  host: yanxuan-partner-money
  subsets:
  - name: internal
    labels:
      app: ${metadata.name}
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRole
metadata:
  name: ${metadata.name}-${metadata.namespace}
  namespace: qz
spec:
  rules:
  - services:
    - qz-egress.qz.svc.cluster.local
    methods:
    - GET
    - HEAD
    constraints:
    - key: "request.headers[:authority]"
      values: ["${metadata.name!}"]
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRoleBinding
metadata:
  name: ${metadata.name!}-${metadata.namespace!}-whitelist
  namespace: qz
spec:
  subjects:
  <#list nsfExtra.targetList! as svc>
    - user: cluster.local/ns/${svc.namespace!}/sa/${svc.name!}
  </#list>
  roleRef:
    kind: ServiceRole
    name: ${metadata.name!}-${metadata.namespace!}
---
# 白名单配置
apiVersion: "rbac.istio.io/v1alpha1"
kind: ServiceRole
<#include "common/metadata.ftl"/>
spec:
  rules:
  - services: ["${metadata.name!}.${metadata.namespace!}.svc.cluster.local"]
    methods: ["GET", "HEAD"]
---
apiVersion: "rbac.istio.io/v1alpha1"
kind: ServiceRoleBinding
metadata:
  name: ${metadata.name!}-whitelist
  namespace: ${metadata.namespace!}
spec:
  subjects:
  <#list nsfExtra.targetList! as svc>
    - user: cluster.local/ns/${svc.namespace!}/sa/${svc.name!}
  </#list>
  roleRef:
    kind: ServiceRole
    name: ${metadata.name!}
---
apiVersion: "authentication.istio.io/v1alpha1"
kind: "Policy"
<#include "common/metadata.ftl"/>
spec:
  targets:
  - name: ${metadata.name!}
  peers:
  - mtls:
      mode: STRICT
---
# service account
<#include "inner/whiteList-serviceAccount.ftl"/>