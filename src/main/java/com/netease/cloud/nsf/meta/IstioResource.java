package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.validators.CheckObjectMeta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/16
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"apiVersion", "kind", "metadata", "spec"})
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class IstioResource implements HasMetadata, Serializable {
    @NotNull
    @JsonProperty("apiVersion")
    private String apiVersion = "networking.istio.io/v1alpha3";

    @NotNull
    @JsonProperty("kind")
    private String kind;

    @JsonProperty("metadata")
    @Valid
    @CheckObjectMeta(
            regexp = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$",
            max = 253
    )
    private ObjectMeta metadata;

    @JsonProperty("spec")
    private Map spec;

    public IstioResource() {
    }

    public IstioResource(String apiVersion, String kind, ObjectMeta metadata, Map spec) {
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.spec = spec;
    }

    public IstioResource(String kind, String name, String namespace, Map spec) {
        this.kind = kind;
        this.spec = spec;
        ObjectMeta meta = new ObjectMeta();
        meta.setName(name);
        meta.setNamespace(namespace);
        this.metadata = meta;
    }

    public Map getSpec() {
        return spec;
    }

    public void setSpec(Map spec) {
        this.spec = spec;
    }

    @Override
    public ObjectMeta getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(ObjectMeta objectMeta) {
        this.metadata = objectMeta;
    }

    @Override
    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public void setApiVersion(String s) {
        this.apiVersion = s;
    }
}
