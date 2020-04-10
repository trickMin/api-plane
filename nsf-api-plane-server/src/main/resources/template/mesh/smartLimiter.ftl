apiVersion: microservice.netease.com/v1alpha1
kind: SmartLimiter
metadata:
  name: ${t_smart_limiter_name}
  namespace: ${t_namespace}
spec:
  ratelimitConfig:
    descriptors:
<@indent count=4>${t_smart_limiter_config}</@indent>
