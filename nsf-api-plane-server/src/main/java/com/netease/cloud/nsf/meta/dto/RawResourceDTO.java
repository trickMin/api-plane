package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.HasMetadata;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/3/10
 **/
public class RawResourceDTO {

    @JsonProperty(value = "Resource")
    private HasMetadata resource;

    public HasMetadata getResource() {
        return resource;
    }

    public void setResource(HasMetadata resource) {
        this.resource = resource;
    }
}
