package com.netease.cloud.nsf.meta.dto.sm;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
public class ServiceMeshRateLimitDTO {

    @JsonProperty("Host")
    @NotNull(message = "host")
    private String host;

    @JsonProperty("Plugin")
    private String plugin;

    @JsonProperty("ClusterId")
    private String clusterId;

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

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
