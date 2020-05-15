package com.netease.cloud.nsf.meta;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
public class ServiceMeshRateLimit {

    private String host;

    private String serviceName;

    private String namespace;

    private String plugin;

    public static ServiceMeshRateLimit copy(ServiceMeshRateLimit rateLimit) {
        ServiceMeshRateLimit _rateLimit = new ServiceMeshRateLimit();
        _rateLimit.setServiceName(rateLimit.getServiceName());
        _rateLimit.setNamespace(rateLimit.getNamespace());
        _rateLimit.setPlugin(rateLimit.getPlugin());
        _rateLimit.setHost(rateLimit.getHost());
        return _rateLimit;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
