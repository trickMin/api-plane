package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/19
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PortalServiceDTO {

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

    @JsonProperty(value = "Gateway")
    private String gateway;

    @JsonProperty(value = "Protocol")
    @Pattern(regexp = "(http|https)", message = "protocol")
    private String protocol = "http";

    /**
     * 连续错误数
     */
    @JsonProperty(value = "ConsecutiveErrors")
    @Min(value = 0, message = "consecutive errors")
    private Integer consecutiveErrors;

    /**
     * 基础驱逐时间
     */
    @JsonProperty(value = "BaseEjectionTime")
    @Min(value = 0, message = "base ejection time")
    private Long baseEjectionTime;

    /**
     * 最大驱逐比例
     */
    @JsonProperty(value = "MaxEjectionPercent")
    @Min(value = 0, message = "maxEjectionPercent")
    @Max(value = 100, message = "maxEjectionPercent")
    private Integer maxEjectionPercent;

    /**
     * 健康检查
     */
    @JsonProperty(value = "HealthCheck")
    @Valid
    private HealthCheckDTO healthCheck;

    @NotEmpty(message = "service tag")
    @JsonProperty(value = "ServiceTag")
    private String serviceTag;

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

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getConsecutiveErrors() {
        return consecutiveErrors;
    }

    public void setConsecutiveErrors(Integer consecutiveErrors) {
        this.consecutiveErrors = consecutiveErrors;
    }

    public Long getBaseEjectionTime() {
        return baseEjectionTime;
    }

    public void setBaseEjectionTime(Long baseEjectionTime) {
        this.baseEjectionTime = baseEjectionTime;
    }

    public Integer getMaxEjectionPercent() {
        return maxEjectionPercent;
    }

    public void setMaxEjectionPercent(Integer maxEjectionPercent) {
        this.maxEjectionPercent = maxEjectionPercent;
    }

    public HealthCheckDTO getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheckDTO healthCheck) {
        this.healthCheck = healthCheck;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }
}
