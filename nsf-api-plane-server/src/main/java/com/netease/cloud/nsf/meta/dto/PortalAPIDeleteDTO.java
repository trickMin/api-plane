package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/30
 **/
public class PortalAPIDeleteDTO {

    @NotEmpty(message = "gateway")
    @JsonProperty(value = "Gateway")
    private String gateway;

    /**
     * api唯一标识
     */
    @NotEmpty(message = "api code")
    @JsonProperty(value = "Code")
    private String code;

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
