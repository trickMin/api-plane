package com.netease.cloud.nsf.meta;

import com.netease.cloud.nsf.meta.dto.PortalHealthCheckDTO;
import com.netease.cloud.nsf.meta.dto.PortalOutlierDetectionDTO;
import com.netease.cloud.nsf.meta.dto.PortalServiceConnectionPoolDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/23
 **/
public class ServiceSubset {

    private String name;

    private Map<String, String> labels;

    private TrafficPolicy trafficPolicy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public TrafficPolicy getTrafficPolicy() {
        return trafficPolicy;
    }

    public void setTrafficPolicy(TrafficPolicy trafficPolicy) {
        this.trafficPolicy = trafficPolicy;
    }

    public static class TrafficPolicy {
        /**
         * 负载均衡策略
         */
        private Service.ServiceLoadBalancer loadbalancer;

        /**
         * 主动健康检查
         */
        private PortalHealthCheckDTO healthCheck;

        /**
         * 异常点检测（被动健康检查）
         */
        private PortalOutlierDetectionDTO outlierDetection;

        /**
         * 连接池
         */
        private PortalServiceConnectionPoolDto connectionPool;

        public Service.ServiceLoadBalancer getLoadbalancer() {
            return loadbalancer;
        }

        public void setLoadbalancer(Service.ServiceLoadBalancer loadbalancer) {
            this.loadbalancer = loadbalancer;
        }

        public PortalHealthCheckDTO getHealthCheck() {
            return healthCheck;
        }

        public void setHealthCheck(PortalHealthCheckDTO healthCheck) {
            this.healthCheck = healthCheck;
        }

        public PortalOutlierDetectionDTO getOutlierDetection() {
            return outlierDetection;
        }

        public void setOutlierDetection(PortalOutlierDetectionDTO outlierDetection) {
            this.outlierDetection = outlierDetection;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
