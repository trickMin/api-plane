package com.netease.cloud.nsf.cache.meta;

import com.netease.cloud.nsf.util.Const;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetStatus;


import java.util.List;
import java.util.Map;

public class WorkLoadDTO<T extends HasMetadata> extends K8sResourceDTO {


    protected String serviceName;

    protected String serviceDomain;

    protected List<String> sidecarVersion;

    private String projectCode;

    private String envName;

    private boolean isInMesh;

    private Map<String, String> labels;

    private String statusInfo;

    private String lastUpdateTime;

    private String lastOperationType;


    public WorkLoadDTO(T obj, String serviceName, String clusterId) {
        super(obj, clusterId);
        this.serviceDomain = serviceName;
        this.serviceName = serviceName;
        this.clusterId = clusterId;
        if (obj instanceof Deployment) {
            Deployment deployment = (Deployment) obj;
            DeploymentStatus status = deployment.getStatus();
            String total = getValueOrDefault(deployment.getSpec().getReplicas()).toString();
            String ready = getValueOrDefault(status.getReplicas()).toString();
            statusInfo =  ready + "/" + total;
        } else if (obj instanceof StatefulSet) {
            StatefulSet statefulSet = (StatefulSet) obj;
            StatefulSetStatus status = statefulSet.getStatus();
            String total = getValueOrDefault(statefulSet.getSpec().getReplicas()).toString();
            String ready = getValueOrDefault(status.getReplicas()).toString();
            statusInfo =  ready + "/" + total;
        }
        if (obj.getMetadata() != null) {
            this.labels = obj.getMetadata().getLabels();
        }
        Map<String, String> annotation = obj.getMetadata().getAnnotations();
        if (annotation != null) {
            this.lastUpdateTime = annotation.get(Const.WORKLOAD_UPDATE_TIME_ANNOTATION);
            this.lastOperationType = annotation.get(Const.WORKLOAD_OPERATION_TYPE_ANNOTATION);
        }
    }

    public String getServiceDomain() {
        return serviceDomain;
    }

    public void setServiceDomain(String serviceDomain) {
        this.serviceDomain = serviceDomain;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLastOperationType() {
        return lastOperationType;
    }

    public void setLastOperationType(String lastOperationType) {
        this.lastOperationType = lastOperationType;
    }

    public boolean isInMesh() {
        return isInMesh;
    }

    public void setInMesh(boolean inMesh) {
        isInMesh = inMesh;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
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


    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }



//    private boolean IsInjected(T obj) {
//        String injectAnnotationValue = null;
//        try {
//            if (obj instanceof Deployment) {
//                Deployment deployment = (Deployment) obj;
//                Map<String,String> annotation = deployment
//                        .getSpec()
//                        .getTemplate()
//                        .getMetadata()
//                        .getAnnotations();
//                if (annotation == null||annotation.isEmpty()){
//                    return false;
//                }
//                injectAnnotationValue = annotation.get(Const.ISTIO_INJECT_ANNOTATION);
//            } else if (obj instanceof StatefulSet) {
//                StatefulSet statefulSet = (StatefulSet) obj;
//                Map<String,String> annotation = statefulSet
//                        .getSpec()
//                        .getTemplate()
//                        .getMetadata()
//                        .getAnnotations();
//                if (annotation == null||annotation.isEmpty()){
//                    return false;
//                }
//            }
//            if (!StringUtils.isEmpty(injectAnnotationValue) && injectAnnotationValue.equals(Const.OPTION_TRUE)) {
//                return true;
//            } else {
//                return false;
//            }
//        } catch (Exception e) {
//            return false;
//        }
//
//    }
}
