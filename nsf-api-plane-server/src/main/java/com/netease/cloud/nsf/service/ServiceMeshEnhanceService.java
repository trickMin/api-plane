package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.dto.ServiceMeshCircuitBreakerDTO;
import com.netease.cloud.nsf.meta.dto.sm.ServiceMeshRateLimitDTO;

public interface ServiceMeshEnhanceService {

    void updateRateLimit(ServiceMeshRateLimitDTO rateLimitDTO);

    void updateServiceMeshCircuitBreaker(ServiceMeshCircuitBreakerDTO circuitBreakerDTO);
    void deleteRateLimit(ServiceMeshRateLimitDTO rateLimitDTO);
}
