package com.netease.cloud.nsf.meta;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
public class ServiceMeshRateLimit {

    private String host;

    private String namespace;

    private String plugin;

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
}
