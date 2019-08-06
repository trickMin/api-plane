package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public class Gateway {

    @JsonProperty(value = "Name")
    private String name;

    @JsonProperty(value = "Address")
    private String address;

    @JsonProperty(value = "Labels")
    private List<Map<String, String>> labels;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Map<String, String>> getLabels() {
        return labels;
    }

    public void setLabels(List<Map<String, String>> labels) {
        this.labels = labels;
    }
}
