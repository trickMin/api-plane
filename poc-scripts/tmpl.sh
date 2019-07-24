#!/bin/bash
cmd=$1
shift 1
EXT_WEIGHT=0
IN_WEIGHT=100

while [ $# -ge 1 ] ; do
  case "$1" in
    --show) show=1; shift 1;;
    --white-list) WHITE_LIST=1; shift 1;;
    -t) split=(${2//./ }); T_SVC=${split[0]}; T_NS=${split[1]}; shift 2;;
    -s) svcs=(${2//,/ }); shift 2;;
    -e) EXT_WEIGHT=$2; IN_WEIGHT=`expr 100 - $2`; shift 2;;
    *) echo "unknown parameter $1." ; exit 1 ; break;;
  esac
done

if [ "$WHITE_LIST" = "1" ]; then
  DR_TLS='
  trafficPolicy:
    tls:
      mode: ISTIO_MUTUAL'
  SA="
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: $T_SVC
  namespace: $T_NS"

  for svc in ${svcs[@]}; do
    split=(${svc//./ })
    SRC_SVC=${split[0]}
    SRC_NS=${split[1]}
    USERS="${USERS}
  - user: \"cluster.local/ns/${SRC_NS}/sa/${SRC_SVC}\""
    SA="${SA}
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: ${SRC_SVC}
  namespace: ${SRC_NS}"
  done

fi

if [ "$WHITE_LIST" = "1" ]; then
  EGRESS_USERS=$USERS
else
  EGRESS_USERS="
  - user: \"*\""
fi


CONFIG="
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: ${T_SVC}
  namespace: ${T_NS}
spec:
  hosts:
  - yx-provider
  http:
  - route:
    - destination:
        host: ${T_SVC}
        subset: internal
      weight: ${IN_WEIGHT}
    - destination:
        host: qz-egress.qz.svc.cluster.local
      weight: ${EXT_WEIGHT}
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: ${T_SVC}
  namespace: ${T_NS}
spec:${DR_TLS}
  host: ${T_SVC}
  subsets:
  - name: internal
    labels:
      app: ${T_SVC}"

CONFIG_RBAC="
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRole
metadata:
  name: ${T_SVC}
  namespace: ${T_NS}
spec:
  rules:
  - services:
    - ${T_SVC}.${T_NS}.svc.cluster.local
---
apiVersion: rbac.istio.io/v1alpha1
kind: ServiceRoleBinding
metadata:
  name: ${T_SVC}-whitelist
  namespace: ${T_NS}
spec:
  subjects:${USERS}
  roleRef:
    kind: ServiceRole
    name: ${T_SVC}
---
apiVersion: authentication.istio.io/v1alpha1
kind: Policy
metadata:
  name: ${T_SVC}
  namespace: ${T_NS}
spec:
  targets:
  - name: ${T_SVC}
  peers:
  - mtls:
      mode: STRICT"

if [ "$WHITE_LIST" = "1" ]; then
  CONFIG=$CONFIG$CONFIG_RBAC
fi
if [ "$cmd" = "apply" ];then
  CONFIG=$SA$CONFIG
fi

if [ "$show" = "1" ]; then
  echo "$CONFIG"
elif [ "$cmd" = "apply" ]; then
  echo "$CONFIG" | kubectl apply -f -
elif [ "$cmd" = "delete" ]; then
  echo "$CONFIG" | kubectl delete -f -
fi
