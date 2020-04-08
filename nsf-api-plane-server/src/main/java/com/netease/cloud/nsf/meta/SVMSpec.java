package com.netease.cloud.nsf.meta;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import java.util.Map;

public class SVMSpec {

    @Pattern(regexp = "(Deployment|StatefulSet|Service|LabelSelector)", message = "workLoadType")
    @JsonProperty(value = "WorkLoadType")
    @NotEmpty(message = "workLoadType")
    private String workLoadType;

    @JsonProperty(value = "WorkLoadName")
    private String workLoadName;

    @JsonProperty(value = "ExpectedVersion")
    private String expectedVersion;

    @JsonProperty(value = "IptablesParams")
    private String iptablesParams;

    @JsonProperty(value = "IptablesDetail")
    private String iptablesDetail;

    @JsonProperty(value = "IptablesConfig")
    private IptablesConfig iptablesConfig;

    @JsonProperty(value = "LabelSelector")
	private Map<String, String> labels;

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

    public String getIptablesParams() {
        return iptablesParams;
    }

    public void setIptablesParams(String iptablesParams) {
        this.iptablesParams = iptablesParams;
    }

    public String getIptablesDetail() {
        return iptablesDetail;
    }

    public void setIptablesDetail(String iptablesDetail) {
        this.iptablesDetail = iptablesDetail;
    }

    public IptablesConfig getIptablesConfig() {
        return iptablesConfig;
    }

    public void setIptablesConfig(IptablesConfig iptablesConfig) {
        this.iptablesConfig = iptablesConfig;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
