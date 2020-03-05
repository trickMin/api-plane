package com.netease.cloud.nsf.util.advice;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
public class ValidReturnEntity extends CommonReturnEntity {
    @JsonProperty("Violation")
    private List<Object> violation;

    public ValidReturnEntity() {
    }

    public List<Object> getViolation() {
        return violation;
    }

    public void setViolation(List<Object> violation) {
        this.violation = violation;
    }
}
