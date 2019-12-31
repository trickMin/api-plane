package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/23
 **/
public class ServiceSubsetDTO {

    @JsonProperty(value = "Name")
    @NotEmpty(message = "subset name")
    private String name;

    @JsonProperty(value = "Labels")
    private Map<String, String> labels;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
}
