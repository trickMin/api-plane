package com.netease.cloud.nsf.meta.dto.sm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
public class ServiceMeshRateLimitDTO {

    @JsonProperty("Host")
    private String host;

    @JsonProperty("Plugin")
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
}
