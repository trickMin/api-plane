package com.netease.cloud.nsf.cache.meta;

import io.fabric8.kubernetes.api.model.*;

import java.util.*;

public class PodDto<T extends HasMetadata> extends K8sResourceDto {

    private List<ContainerInfo> containerInfoList = new ArrayList<>();

    public PodDto(T obj, String clusterId) {
        super(obj, clusterId);
        if (obj instanceof Pod) {
            Pod pod = (Pod) obj;
            Map<String, ContainerInfo> containerInfoMap = new HashMap<>();
            // 更新容器资源信息
            pod.getSpec().getContainers().forEach(c -> {
                Objects.requireNonNull(containerInfoMap.computeIfAbsent(c.getName(),
                        ContainerInfo::new))
                        .setRequestResource(c.getResources().getRequests())
                        .setLimitResource(c.getResources().getLimits());
            });
            // 更新容器状态信息
            pod.getStatus().getContainerStatuses().forEach(cs -> {
                Objects.requireNonNull(containerInfoMap.computeIfAbsent(cs.getName(), ContainerInfo::new))
                        .setStatusInfo("status", pod.getStatus().getPhase())
                        .setStatusInfo("restartCount", cs.getRestartCount().toString());
            });

            containerInfoList.addAll(containerInfoMap.values());

        }
    }


    public class ContainerInfo {

        private String containerName;
        private Map<String, String> statusInfo = new HashMap<>();
        private Map<String, String> resourceRequest = new HashMap<>();
        private Map<String, String> resourceLimit = new HashMap<>();


        public ContainerInfo(String containerName) {
            this.containerName = containerName;
        }

        public ContainerInfo setStatusInfo(String key, String value) {
            statusInfo.put(key, value);
            return this;
        }

        public ContainerInfo setRequestResource(Map<String, Quantity> resourceMap) {
            if (resourceMap == null || resourceMap.isEmpty()) {
                return this;
            }
            for (Map.Entry<String, Quantity> entry : resourceMap.entrySet()) {
                resourceRequest.put(entry.getKey(), entry.getValue().getAmount());
            }
            return this;
        }

        public ContainerInfo setLimitResource(Map<String, Quantity> resourceMap) {
            if (resourceMap == null || resourceMap.isEmpty()) {
                return this;
            }
            for (Map.Entry<String, Quantity> entry : resourceMap.entrySet()) {
                resourceLimit.put(entry.getKey(), entry.getValue().getAmount());
            }
            return this;
        }


    }
}
