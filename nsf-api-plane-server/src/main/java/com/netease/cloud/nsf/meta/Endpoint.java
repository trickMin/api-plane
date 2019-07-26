package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class Endpoint {

    @JsonProperty("service")
    private InnerService service;

    public InnerService getService() {
        return service;
    }

    public void setService(InnerService service) {
        this.service = service;
    }
}
