package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class PortalOutlierDetectionDTO {

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
}
