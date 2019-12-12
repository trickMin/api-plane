package com.netease.cloud.nsf.cache.meta;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangzihao
 */
public class PodDTO<T extends HasMetadata> extends K8sResourceDTO {

    private static final String RESOURCE_VALUE_PATTERN = "[\\d]+(?=\\D*)";
    private static final String LIMIT_CPU_FORMAT = "%.1f Cores";
    private static final String LIMIT_MEMORY_FORMAT = "%.0f MiB";

    private String hostIp;

    private String podIp;

    private String status;

    private String totalLimitCpu;

    private float totalLimitCpuValue;

    private String totalLimitMemory;

    private float totalLimitMemoryValue;

    private int totalRestartCount;

    private String sidecarStatus;


    private List<ContainerInfo> containerInfoList = new ArrayList<>();

    public String getSidecarStatus() {
        return sidecarStatus;
    }

    public void setSidecarStatus(String sidecarStatus) {
        this.sidecarStatus = sidecarStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getPodIp() {
        return podIp;
    }

    public void setPodIp(String podIp) {
        this.podIp = podIp;
    }

    public PodDTO(T obj, String clusterId) {
        super(obj, clusterId);
        if (obj instanceof Pod) {
            Pod pod = (Pod) obj;
            this.hostIp = pod.getStatus().getHostIP();
            this.podIp = pod.getStatus().getPodIP();
            this.status = pod.getStatus().getPhase();
            Map<String, ContainerInfo> containerInfoMap = new HashMap<>();
            // 更新容器资源信息

            pod.getSpec().getContainers().forEach(c -> {
                Objects.requireNonNull(containerInfoMap.computeIfAbsent(c.getName(),
                        ContainerInfo::new))
                        .setRequestResource(c.getResources().getRequests())
                        .setLimitResource(c.getResources().getLimits());

                this.totalLimitCpuValue += getLimitCpuValue(c.getResources().getLimits());
                this.totalLimitMemoryValue += getLimitMeValue(c.getResources().getLimits());
            });
            // 更新容器状态信息
            pod.getStatus().getContainerStatuses().forEach(cs -> {
                Objects.requireNonNull(containerInfoMap.computeIfAbsent(cs.getName(), ContainerInfo::new))
                        .setStatusInfo("restartCount", cs.getRestartCount().toString());
                totalRestartCount += cs.getRestartCount();
            });

            this.totalLimitCpu = String.format(LIMIT_CPU_FORMAT, this.totalLimitCpuValue);
            this.totalLimitMemory = String.format(LIMIT_MEMORY_FORMAT, this.totalLimitMemoryValue);
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


    private float getValueFromAmount(String amount, int index) {
        if (index < 0) {
            return 0;
        }
        return Float.parseFloat(amount.substring(0, index));
    }

    private String getUnitFromAmount(String amount, int index) {
        if (index < 0) {
            return "";
        }
        return amount.substring(index);
    }

    private float getLimitCpuValue(Map<String, Quantity> resourceMap) {
        if (resourceMap == null || resourceMap.isEmpty()) {
            return 0;
        }
        if (resourceMap.get("cpu") != null) {
            String amount = resourceMap.get("cpu").getAmount();
            int limitValueIndex = getResourceValueIndex(amount);
            if (limitValueIndex < 0) {
                return 0;
            }
            String unit = getUnitFromAmount(amount, limitValueIndex);
            float value = getValueFromAmount(amount, limitValueIndex);
            if (unit.contains("m")) {
                value = value / 1000;
            }
            return value;
        } else {
            return 0;
        }
    }

    private float getLimitMeValue(Map<String, Quantity> resourceMap) {
        if (resourceMap == null || resourceMap.isEmpty()) {
            return 0;
        }
        if (resourceMap.get("memory") != null) {
            String amount = resourceMap.get("memory").getAmount();
            int limitValueIndex = getResourceValueIndex(amount);
            if (limitValueIndex < 0) {
                return 0;
            }
            String unit = getUnitFromAmount(amount, limitValueIndex);
            float value = getValueFromAmount(amount, limitValueIndex);
            if (unit.contains("G")) {
                value = value * 1024;
            }
            return value;
        } else {
            return 0;
        }
    }


    private int getResourceValueIndex(String amount) {
        if (StringUtils.isEmpty(amount)) {
            return -1;
        }
        Pattern p = Pattern.compile(RESOURCE_VALUE_PATTERN);
        Matcher matcher = p.matcher(amount);
        if (matcher.find()) {
            return matcher.end();
        }
        return -1;
    }

}
