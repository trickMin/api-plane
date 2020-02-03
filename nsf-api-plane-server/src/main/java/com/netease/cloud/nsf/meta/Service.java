package com.netease.cloud.nsf.meta;


import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/20
 **/
public class Service extends CommonModel {

    private String code;

    /**
     * 对应后端服务
     */
    private String backendService;

    /**
     * 类型
     */
    private String type;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 所属网关
     */
    private String gateway;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 连续错误数
     */
    private Integer consecutiveErrors;

    /**
     * 基础驱逐时间
     */
    private Long baseEjectionTime;

    /**
     * 最大驱逐比例
     */
    private Integer maxEjectionPercent;

    /**
     * 健康检查路径
     */
    private String path;

    /**
     * 健康检查超时时间
     */
    private Long timeout;

    /**
     * 期望响应码
     */
    private List<Integer> expectedStatuses;

    /**
     * 健康实例检查间隔
     */
    private Long healthyInterval;

    /**
     * 健康阈值
     */
    private Integer healthyThreshold;

    /**
     * 异常实例检查间隔
     */
    private Long unhealthyInterval;

    /**
     * 异常实例阈值
     */
    private Integer unhealthyThreshold;

    /**
     * 服务标签，唯一
     */
    private String serviceTag;

    /**
     * 负载均衡
     */
    private ServiceLoadBalancer loadBalancer;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 服务subsets
     */
    private List<ServiceSubset> subsets;

    private String subset;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBackendService() {
        return backendService;
    }

    public void setBackendService(String backendService) {
        this.backendService = backendService;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getConsecutiveErrors() {
        return consecutiveErrors;
    }

    public void setConsecutiveErrors(Integer consecutiveErrors) {
        this.consecutiveErrors = consecutiveErrors;
    }

    public Long getBaseEjectionTime() {
        return baseEjectionTime;
    }

    public void setBaseEjectionTime(Long baseEjectionTime) {
        this.baseEjectionTime = baseEjectionTime;
    }

    public Integer getMaxEjectionPercent() {
        return maxEjectionPercent;
    }

    public void setMaxEjectionPercent(Integer maxEjectionPercent) {
        this.maxEjectionPercent = maxEjectionPercent;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public List<Integer> getExpectedStatuses() {
        return expectedStatuses;
    }

    public void setExpectedStatuses(List<Integer> expectedStatuses) {
        this.expectedStatuses = expectedStatuses;
    }

    public Long getHealthyInterval() {
        return healthyInterval;
    }

    public void setHealthyInterval(Long healthyInterval) {
        this.healthyInterval = healthyInterval;
    }

    public Integer getHealthyThreshold() {
        return healthyThreshold;
    }

    public void setHealthyThreshold(Integer healthyThreshold) {
        this.healthyThreshold = healthyThreshold;
    }

    public Long getUnhealthyInterval() {
        return unhealthyInterval;
    }

    public void setUnhealthyInterval(Long unhealthyInterval) {
        this.unhealthyInterval = unhealthyInterval;
    }

    public Integer getUnhealthyThreshold() {
        return unhealthyThreshold;
    }

    public void setUnhealthyThreshold(Integer unhealthyThreshold) {
        this.unhealthyThreshold = unhealthyThreshold;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public ServiceLoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(ServiceLoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<ServiceSubset> getSubsets() {
        return subsets;
    }

    public void setSubsets(List<ServiceSubset> subsets) {
        this.subsets = subsets;
    }

    public String getSubset() {
        return subset;
    }

    public void setSubset(String subset) {
        this.subset = subset;
    }

    public static class ServiceLoadBalancer {
        private String simple;
        private ConsistentHash consistentHash;

        public String getSimple() {
            return simple;
        }

        public void setSimple(String simple) {
            this.simple = simple;
        }

        public ConsistentHash getConsistentHash() {
            return consistentHash;
        }

        public void setConsistentHash(ConsistentHash consistentHash) {
            this.consistentHash = consistentHash;
        }

        public static class ConsistentHash {

            private String httpHeaderName;
            private Boolean useSourceIp;
            private HttpCookie httpCookie;

            public static class HttpCookie {
                private String name;
                private String path;
                private String ttl;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getPath() {
                    return path;
                }

                public void setPath(String path) {
                    this.path = path;
                }

                public String getTtl() {
                    return ttl;
                }

                public void setTtl(String ttl) {
                    this.ttl = ttl;
                }
            }

            public String getHttpHeaderName() {
                return httpHeaderName;
            }

            public void setHttpHeaderName(String httpHeaderName) {
                this.httpHeaderName = httpHeaderName;
            }

            public Boolean getUseSourceIp() {
                return useSourceIp;
            }

            public void setUseSourceIp(Boolean useSourceIp) {
                this.useSourceIp = useSourceIp;
            }

            public HttpCookie getHttpCookie() {
                return httpCookie;
            }

            public void setHttpCookie(HttpCookie httpCookie) {
                this.httpCookie = httpCookie;
            }
        }

    }
}
