package com.netease.cloud.nsf.core.plugin.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/7
 **/
public class Matcher {
    @JsonProperty("source_type")
    private String sourceType;
    @JsonProperty("left_value")
    private String leftValue;
    @JsonProperty("op")
    private String op;
    @JsonProperty("right_value")
    private String rightValue;

    public String getSourceType() {
        return sourceType;
    }

    @JsonProperty("source_type")
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getLeftValue() {
        return leftValue;
    }

    @JsonProperty("left_value")
    public void setLeftValue(String leftValue) {
        this.leftValue = leftValue;
    }

    public String getOp() {
        return op;
    }

    @JsonProperty("op")
    public void setOp(String op) {
        this.op = op;
    }

    public String getRightValue() {
        return rightValue;
    }

    @JsonProperty("right_value")
    public void setRightValue(String rightValue) {
        this.rightValue = rightValue;
    }
}
