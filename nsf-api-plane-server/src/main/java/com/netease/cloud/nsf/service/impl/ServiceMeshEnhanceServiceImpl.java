package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.servicemesh.ServiceMeshConfigManager;
import com.netease.cloud.nsf.meta.ServiceMeshCircuitBreaker;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;
import com.netease.cloud.nsf.meta.dto.ServiceMeshCircuitBreakerDTO;
import com.netease.cloud.nsf.meta.dto.sm.ServiceMeshRateLimitDTO;
import com.netease.cloud.nsf.service.ServiceMeshEnhanceService;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
@Service
public class ServiceMeshEnhanceServiceImpl implements ServiceMeshEnhanceService {

    ServiceMeshConfigManager configManager;

    @Autowired
    public ServiceMeshEnhanceServiceImpl(ServiceMeshConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void updateRateLimit(ServiceMeshRateLimitDTO rateLimitDTO) {
        ServiceMeshRateLimit rateLimit = rateLimitDTO2RateLimit(rateLimitDTO);
        configManager.updateRateLimit(rateLimit);
    }

    @Override
    public void deleteRateLimit(ServiceMeshRateLimitDTO rateLimitDTO) {
        ServiceMeshRateLimit rateLimit = rateLimitDTO2RateLimit(rateLimitDTO);
        configManager.deleteRateLimit(rateLimit);
    }

    @Override
    public void updateServiceMeshCircuitBreaker(ServiceMeshCircuitBreakerDTO circuitBreakerDTO) {
        ServiceMeshCircuitBreaker circuitBreaker = circuitBreakerDTO2CircuitBreaker(circuitBreakerDTO);
        configManager.updateCircuitBreaker(circuitBreaker);
    }

    private ServiceMeshCircuitBreaker circuitBreakerDTO2CircuitBreaker(ServiceMeshCircuitBreakerDTO circuitBreakerDTO) {
        ServiceMeshCircuitBreaker serviceMeshCircuitBreaker = new ServiceMeshCircuitBreaker();
        serviceMeshCircuitBreaker.setHost(circuitBreakerDTO.getHost());
        serviceMeshCircuitBreaker.setRouterName(circuitBreakerDTO.getRouterName());
        serviceMeshCircuitBreaker.setPlugins(circuitBreakerDTO.getPlugins());
        serviceMeshCircuitBreaker.setStatus(circuitBreakerDTO.getStatus());
        serviceMeshCircuitBreaker.setNamespace(circuitBreakerDTO.getNamespace());
        return serviceMeshCircuitBreaker;
    }

    private ServiceMeshRateLimit rateLimitDTO2RateLimit(ServiceMeshRateLimitDTO rateLimitDTO) {
        ServiceMeshRateLimit rl = new ServiceMeshRateLimit();

        String host = rateLimitDTO.getHost();
        if (!host.contains(".")) {
            throw new ApiPlaneException("illegal argument host " + host);
        }
        String[] metas = host.split("\\.");
        rl.setHost(host);
        rl.setNamespace(metas[1]);
        rl.setPlugin(rateLimitDTO.getPlugin());
        return rl;
    }
}
