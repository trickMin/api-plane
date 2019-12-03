package com.netease.cloud.nsf.meta;


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Pattern;

public class SVMSpec {

    @Pattern(regexp = "(Deployment|Statefulset|Service|Labelselector)", message = "type")
    @JsonProperty(value = "WorkLoadType")
    private String workLoadType;

    @JsonProperty(value = "WorkLoadName")
    private String workLoadName;

    @JsonProperty(value = "ExpectedVersion")
    private String expectedVersion;

    public String getWorkLoadType() {
        return workLoadType;
    }

    public void setWorkLoadType(String workLoadType) {
        this.workLoadType = workLoadType;
    }

    public String getWorkLoadName() {
        return workLoadName;
    }

    public void setWorkLoadName(String workLoadName) {
        this.workLoadName = workLoadName;
    }

    public String getExpectedVersion() {
        return expectedVersion;
    }

    public void setExpectedVersion(String expectedVersion) {
        this.expectedVersion = expectedVersion;
    }
}
