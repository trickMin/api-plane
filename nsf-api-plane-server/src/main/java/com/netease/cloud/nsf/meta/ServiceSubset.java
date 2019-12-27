package com.netease.cloud.nsf.meta;

import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/23
 **/
public class ServiceSubset {

    private String name;

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
