apiVersion: networking.istio.io/v1alpha3
kind: VersionManager
metadata:
  name: version-manager
  namespace: ${t_namespace}
spec:
  defaultVersion: envoy
  retryPolicy:
    neverRetry: false
    retryTime: 5
    retryInterval: 3s
  sidecarVersionSpec:
<#list t_version_manager_workloads! as w>
  - expectedVersion: ${w.expectedVersion}
    podsHash: none
    <#if w.workLoadType == "deployment">
    viaDeployment:
    </#if>
    <#if w.workLoadType == "statefulset">
    viaStatefulSet:
    </#if>
    <#if w.workLoadType == "service">
    viaService:
    </#if>
    <#if w.workLoadType == "labelselector">
    viaLabelSelector:
    </#if>
      name: ${w.workLoadName}
</#list>