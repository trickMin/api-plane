#黑白名单，使用com.netease.cloud.nsf.meta.WhiteList进行填充
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRole
metadata:
  name: qz-ingress-whitelist
  namespace: ${namespace}
spec:
  rules:
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRoleBinding
metadata:
  name: qz-ingress-whitelist
  namespace: ${namespace}
spec:
  subjects:
  - user: "cluster.local/ns/${namespace}/sa/istio-ingressgateway-service-account"
  roleRef:
    kind: ServiceRole
    name: qz-ingress-whitelist
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRole
metadata:
name: qz-ingress-passed
namespace: ${namespace}
spec:
rules:
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRoleBinding
metadata:
name: qz-ingress-passed
namespace: ${namespace}
spec:
subjects:
- user: "cluster.local/ns/${namespace}/sa/istio-ingressgateway-service-account"
roleRef:
kind: ServiceRole
name: qz-ingress-passed


