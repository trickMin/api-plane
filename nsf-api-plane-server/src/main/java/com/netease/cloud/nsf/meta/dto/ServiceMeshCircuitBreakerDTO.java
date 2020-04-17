package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;

/**
 * @author zhangzihao
 */
public class ServiceMeshCircuitBreakerDTO {


    @JsonProperty(value = "Host")
    @NotBlank
    private String host;
    @JsonProperty(value = "RouterName")
    @NotBlank
    private String routerName;
    @JsonProperty(value = "Plugins")
    @NotBlank
    private String plugins;
    @JsonProperty(value = "Status")
    private Integer status;
    @JsonProperty(value = "Namespace")
    private String namespace;


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getRouterName() {
        return routerName;
    }

    public void setRouterName(String routerName) {
        this.routerName = routerName;
    }

    public String getPlugins() {
        return plugins;
    }

    public void setPlugins(String plugins) {
        this.plugins = plugins;
    }

    @Override
    public String toString() {
        return "ServiceMeshCircuitBreakerDTO{" +
                "host='" + host + '\'' +
                ", routerName='" + routerName + '\'' +
                ", plugins='" + plugins + '\'' +
                ", status=" + status +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
