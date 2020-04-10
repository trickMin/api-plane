apiVersion: microservice.netease.com/v1alpha1
kind: SmartLimiter
metadata:
  name: ${t_smart_limiter_name}
  namespace: ${t_namespace}
spec:
  ${t_smart_limit_config}
