package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.Valid;

/**
 * @author Kent
 */
public class PortalTrafficPolicyDTO {

    /**
     * 负载均衡策略
     */
    @JsonProperty(value = "LoadBalancer")
    @Valid
    private PortalLoadBalancerDTO loadbalancer;

    /**
     * 主动健康检查
     */
    @JsonProperty(value = "HealthCheck")
    @Valid
    private PortalHealthCheckDTO healthCheck;

    /**
     * 异常点检测（被动健康检查）
     */
    @JsonProperty(value = "OutlierDetection")
    @Valid
    private PortalOutlierDetectionDTO outlierDetection;

    /**
     * 连接池
     */
    @Valid
    @JsonProperty(value = "ConnectionPool")
    private PortalServiceConnectionPoolDto connectionPool;

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

    public PortalServiceConnectionPoolDto getConnectionPool() {
        return connectionPool;
    }

    public void setConnectionPool(PortalServiceConnectionPoolDto connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
