package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.HasMetadata;

public class ResourceWrapperDTO {

    @JsonProperty(value = "Resource")
    private HasMetadata resource;

    @JsonProperty(value = "ClusterId")
    private String clusterId;

    @JsonProperty(value = "Name")
    private String name;

    @JsonProperty(value = "Namespace")
    private String namespace;

    @JsonProperty(value = "Kind")
    private String kind;

    @JsonProperty(value = "CreateTime")
    private String createTime;

    public ResourceWrapperDTO(HasMetadata resource, String clusterId) {
        this.resource = resource;
        this.clusterId = clusterId;
        this.name = resource.getMetadata().getName();
        this.namespace = resource.getMetadata().getNamespace();
        this.kind = resource.getKind();
        this.createTime = resource.getMetadata().getCreationTimestamp();
    }

    public HasMetadata getResource() {
        return resource;
    }

    public void setResource(HasMetadata resource) {
        this.resource = resource;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
