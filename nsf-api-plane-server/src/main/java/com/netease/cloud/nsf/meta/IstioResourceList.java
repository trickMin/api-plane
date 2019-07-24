package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ListMeta;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/16
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"apiVersion", "kind", "metadata", "items"})
public class IstioResourceList implements KubernetesResource, KubernetesResourceList {
    @NotNull
    @JsonProperty("apiVersion")
    private String apiVersion = "networking.istio.io/v1alpha3";

    @JsonProperty("items")
    @Valid
    private List<IstioResource> items = new ArrayList();

    @NotNull
    @JsonProperty("kind")
    private String kind = "IstioResouceList";

    @JsonProperty("metadata")
    @Valid
    private ListMeta metadata;

    public IstioResourceList() {
    }

    public IstioResourceList(String apiVersion, List<IstioResource> items, String kind, ListMeta metadata) {
        this.apiVersion = apiVersion;
        this.items = items;
        this.kind = kind;
        this.metadata = metadata;
    }

    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return this.apiVersion;
    }

    @JsonProperty("apiVersion")
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }


    @JsonProperty("items")
    @Override
    public List<IstioResource> getItems() {
        return this.items;
    }

    @JsonProperty("items")
    public void setItems(List<IstioResource> items) {
        this.items = items;
    }

    @JsonProperty("kind")
    public String getKind() {
        return this.kind;
    }

    @JsonProperty("kind")
    public void setKind(String kind) {
        this.kind = kind;
    }

    @JsonProperty("metadata")
    @Override
    public ListMeta getMetadata() {
        return this.metadata;
    }

    @JsonProperty("metadata")
    public void setMetadata(ListMeta metadata) {
        this.metadata = metadata;
    }

}
