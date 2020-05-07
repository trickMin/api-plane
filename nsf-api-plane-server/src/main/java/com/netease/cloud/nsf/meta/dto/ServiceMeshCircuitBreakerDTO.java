package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zhangzihao
 */
public class ServiceMeshCircuitBreakerDTO {


    @JsonProperty(value = "Host")
    @NotNull
    private String host;
    @JsonProperty(value = "RouterName")
    @NotNull
    private String routerName;
    @JsonProperty(value = "Plugins")
    @NotNull
    private String plugins;
    @JsonProperty(value = "Status")
    private Integer status;
    @JsonProperty(value = "Namespace")
    private String namespace;
    @JsonProperty(value = "ruleType")
    private String ruleType;

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

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
