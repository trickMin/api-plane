package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/26
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerService {

    @JsonProperty("hostname")
    private String hostname;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
