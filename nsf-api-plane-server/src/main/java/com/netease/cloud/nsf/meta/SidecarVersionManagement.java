package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class SidecarVersionManagement {

    @NotEmpty(message = "clusterId")
    @JsonProperty(value = "ClusterId")
    private String clusterId;

    @NotEmpty(message = "namespace")
    @JsonProperty(value = "Namespace")
    private String namespace;

    @NotEmpty(message = "workLoads")
    @JsonProperty(value = "WorkLoads")
    private List<SVMSpec> workLoads;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<SVMSpec> getWorkLoads() {
        return workLoads;
    }

    public void setWorkLoads(List<SVMSpec> workLoads) {
        this.workLoads = workLoads;
    }
}
