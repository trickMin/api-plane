package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

/**
 * @author yutao04
 * @date 2021.12.07
 *
 * 插件实体类
 */
public class GatewayPluginDTO {

    @JsonProperty(value = "Plugins")
    private List<String> plugins;

    @JsonProperty(value = "RouteId")
    private String routeId;

    @JsonProperty(value = "PluginType")
    private String pluginType;

    @NotEmpty(message = "Hosts")
    @JsonProperty(value = "Hosts")
    private List<String> hosts;

    @NotEmpty(message = "Gateways")
    @JsonProperty(value = "Gateways")
    private List<String> gateways;

    @JsonProperty(value = "Code")
    private String code;

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public List<String> getGateways() {
        return gateways;
    }

    public void setGateways(List<String> gateways) {
        this.gateways = gateways;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }
}
