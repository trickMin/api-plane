package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/28
 **/
public class ServiceHealth {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Endpoints")
    private List<EndpointHealth> eps;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EndpointHealth> getEps() {
        return eps;
    }

    public void setEps(List<EndpointHealth> eps) {
        this.eps = eps;
    }
}
