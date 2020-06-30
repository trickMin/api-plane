package com.netease.cloud.nsf.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;
import java.util.Map;

public class ResourceUpdateEvent<T extends HasMetadata> {

    private T resourceObject;
    private List<T> resourceList;
    private String eventType;
    private String clusterId;
    private String namespace;
    private String name;
    private Map<String, String> labels;
    private Long resourceVersion;
    private String kind;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public T getResourceObject() {
        return resourceObject;
    }

    public void setResourceObject(T resourceObject) {
        this.resourceObject = resourceObject;
    }

    public List<T> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<T> resourceList) {
        this.resourceList = resourceList;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(Long resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public static class Builder<T extends HasMetadata> {

        private T resourceObject;
        private List<T> resourceList;
        private String eventType;
        private String clusterId;
        private String namespace;
        private Map<String, String> labels;
        private String name;
        private Long resourceVersion;
        private String kind;

        public Builder addKind(String kind){
            this.kind = kind;
            return this;
        }

        public Builder addResourceVersion(Long version){
            this.resourceVersion = version;
            return this;
        }

        public Builder addEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder addResourceObj(T resourceObject) {
            this.resourceObject = resourceObject;
            return this;
        }

        public Builder addCluster(String clusterId) {
            this.clusterId = clusterId;
            return this;
        }

        public Builder addNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder addLabels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder addResourceList(List<T> resourceList) {
            this.resourceList = resourceList;
            return this;
        }

        public Builder addName(String name) {
            this.name = name;
            return this;
        }

        public ResourceUpdateEvent<T> build() {
            ResourceUpdateEvent<T> resourceUpdateEvent = new ResourceUpdateEvent<>();
            resourceUpdateEvent.setClusterId(this.clusterId);
            resourceUpdateEvent.setEventType(this.eventType);
            resourceUpdateEvent.setNamespace(this.namespace);
            resourceUpdateEvent.setResourceObject(this.resourceObject);
            resourceUpdateEvent.setResourceList(this.resourceList);
            resourceUpdateEvent.setName(this.name);
            resourceUpdateEvent.setResourceVersion(this.resourceVersion);
            resourceUpdateEvent.setKind(this.kind);
            return resourceUpdateEvent;
        }


    }
}