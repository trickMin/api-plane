package com.netease.cloud.nsf.cache.meta;

import io.fabric8.kubernetes.api.model.HasMetadata;

public class K8sResourceDTO<T extends HasMetadata> {

    protected String name;

    protected String kind;

    protected String namespace;

    protected String clusterId;

    protected String createTime;

    public K8sResourceDTO() {
    }

    public K8sResourceDTO(T obj, String clusterId){
        this.kind = obj.getKind();
        this.name = obj.getMetadata().getName();
        this.namespace = obj.getMetadata().getNamespace();
        this.createTime = obj.getMetadata().getCreationTimestamp();
        this.clusterId = clusterId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    protected Object getValueOrDefault(Object value){
        if (value == null){
            return "";
        }
        return value;
    }
}
