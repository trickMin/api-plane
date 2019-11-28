package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

public class PodVersion {

    @JsonProperty(value = "ClusterIP")
    private String clusterIP;

    @NotEmpty(message = "namespace")
    @JsonProperty(value = "Namespace")
    private String namespace;

    @NotEmpty(message = "podnames")
    @JsonProperty(value = "PodNames")
    private List<String> podNames;

    public String getClusterIP() {
        return clusterIP;
    }

    public void setClusterIP(String clusterIP) {
        this.clusterIP = clusterIP;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getPodNames() {
        return podNames;
    }

    public void setPodNames(List<String> podNames) {
        this.podNames = podNames;
    }
}
