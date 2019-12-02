package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/28
 **/
public class EndpointHealth {

    @JsonProperty("Address")
    private String address;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Port")
    private Integer port;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
