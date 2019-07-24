apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
<#include "common/metadata.ftl"/>
spec:
  hosts:
  - ${nsfExtra.host!}
  http:
  - route:
<#list nsfExtra.destinations! as des>
    - destination:
        host: ${nsfExtra.host!}
        subset: ${des.subset!}
      weight: ${des.weight!}
</#list>