package com.netease.cloud.nsf.cache.meta;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkLoadDTO<T extends HasMetadata> extends K8sResourceDTO {


    protected String serviceName;

    protected String serviceDomain;

    protected List<String> sidecarVersion;

    private Map<String, String> statusInfo = new HashMap<>();


    public WorkLoadDTO(T obj, String serviceName, String clusterId) {
        super(obj, clusterId);
        this.serviceDomain = serviceName;
        this.serviceName = serviceName;
        this.clusterId = clusterId;
        if (obj instanceof Deployment) {
            Deployment deployment = (Deployment) obj;
            DeploymentStatus status = deployment.getStatus();
            statusInfo.put("desired", getValueOrDefault(deployment.getSpec().getReplicas()).toString());
            statusInfo.put("current", getValueOrDefault(status.getReplicas()).toString());
            statusInfo.put("up-to-date", getValueOrDefault(status.getUpdatedReplicas()).toString());
            statusInfo.put("available", getValueOrDefault(status.getAvailableReplicas()).toString());
        } else if (obj instanceof StatefulSet) {
            StatefulSet statefulSet = (StatefulSet) obj;
            StatefulSetStatus status = statefulSet.getStatus();
            statusInfo.put("desired", getValueOrDefault(statefulSet.getSpec().getReplicas()).toString());
            statusInfo.put("current", getValueOrDefault(status.getReplicas()).toString());
            statusInfo.put("up-to-date", getValueOrDefault(status.getUpdatedReplicas()).toString());
        }
    }

    public List<String> getSidecarVersion() {
        return sidecarVersion;
    }

    public void setSidecarVersion(List<String> sidecarVersion) {
        this.sidecarVersion = sidecarVersion;
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


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    public Map<String, String> getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(Map<String, String> statusInfo) {
        this.statusInfo = statusInfo;
    }


}
