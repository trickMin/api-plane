package com.netease.cloud.nsf.meta;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

public class SVMSpec {

    @Pattern(regexp = "(Deployment|StatefulSet|Service|Labelselector)", message = "workLoadType")
    @JsonProperty(value = "WorkLoadType")
    @NotEmpty(message = "workLoadType")
    private String workLoadType;

    @JsonProperty(value = "WorkLoadName")
    @NotEmpty(message = "workLoadName")
    private String workLoadName;

    @JsonProperty(value = "ExpectedVersion")
    @NotEmpty(message = "expectedVersion")
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
