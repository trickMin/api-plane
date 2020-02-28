package com.netease.cloud.nsf.cache.meta;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;

import java.util.Map;

public class ServiceDto<T extends HasMetadata> extends K8sResourceDTO {

    private Map<String, String> labels;

    private Map<String, String> selectLabels;

    private String appName;


    public ServiceDto(T obj, String clusterId) {
        super(obj,clusterId);
        Service service = (Service)obj;
        this.labels = service.getMetadata().getLabels();
        this.selectLabels = service.getSpec().getSelector();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getSelectLabels() {
        return selectLabels;
    }

    public void setSelectLabels(Map<String, String> selectLabels) {
        this.selectLabels = selectLabels;
    }
}
