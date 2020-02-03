package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;

public class PortalTrafficPolicyDTO {

    @JsonProperty(value = "LoadBalancer")
    @Valid
    private PortalLoadBalancerDTO loadbalancer;

    @JsonProperty(value = "OutlierDetection")
    @Valid
    private PortalOutlierDetectionDTO outlierDetection;

    /**
     * 健康检查
     */
    @JsonProperty(value = "HealthCheck")
    @Valid
    private PortalHealthCheckDTO healthCheck;

    public PortalLoadBalancerDTO getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(PortalLoadBalancerDTO loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public PortalOutlierDetectionDTO getOutlierDetection() {
        return outlierDetection;
    }

    public void setOutlierDetection(PortalOutlierDetectionDTO outlierDetection) {
        this.outlierDetection = outlierDetection;
    }

    public PortalHealthCheckDTO getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(PortalHealthCheckDTO healthCheck) {
        this.healthCheck = healthCheck;
    }
}
