package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/26
 **/
public class PortalRouteServiceDTO {

    /**
     * 服务唯一标识
     */
    @NotEmpty(message = "code")
    @JsonProperty(value = "Code")
    private String code;

    /**
     * 对应后端服务
     */
    @NotEmpty(message = "backend service")
    @JsonProperty(value = "BackendService")
    private String backendService;

    /**
     * 类型
     */
    @NotEmpty(message = "type")
    @JsonProperty(value = "Type")
    @Pattern(regexp = "(STATIC|DYNAMIC)", message = "type")
    private String type;

    /**
     * 权重
     */
    @JsonProperty(value = "Weight")
    private Integer weight;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBackendService() {
        return backendService;
    }

    public void setBackendService(String backendService) {
        this.backendService = backendService;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
