package com.netease.cloud.nsf.cache.meta;

import io.fabric8.kubernetes.api.model.*;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangzihao
 */
public class PodDto<T extends HasMetadata> extends K8sResourceDto {

    private static final String RESOURCE_VALUE_PATTERN = "[\\d]+(?=\\D*)";
    private static final String RESOURCE_UNIT_PATTERN = "(?<=\\d)\\D*";

    private String hostIp;

    private String podIp;

    private String status;

    private String totalLimitCpu;

    private String totalLimitMemory;

    private int totalRestartCount;


    private List<ContainerInfo> containerInfoList = new ArrayList<>();

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

    public PodDto(T obj, String clusterId) {
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
            });
            // 更新容器状态信息
            pod.getStatus().getContainerStatuses().forEach(cs -> {
                Objects.requireNonNull(containerInfoMap.computeIfAbsent(cs.getName(), ContainerInfo::new))
                        .setStatusInfo("restartCount", cs.getRestartCount().toString());
                totalRestartCount += cs.getRestartCount();
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
            float totalLimitCpuValue = 0;
            float totalLimitMemoryValue = 0;
            for (Map.Entry<String, Quantity> entry : resourceMap.entrySet()) {
                resourceLimit.put(entry.getKey(), entry.getValue().getAmount());

                float limitCpuValue = getResourceValue(resourceMap.get("cpu"));
                String limitCpuUnit = getResourceUnit(resourceMap.get("cpu"));
                if (limitCpuUnit.contains("m")) {
                    limitCpuValue = limitCpuValue / 1000;
                }
                float limitMemoryValue = getResourceValue(resourceMap.get("memory"));
                String limitMemoryUnit = getResourceUnit(resourceMap.get("memory"));
                if (limitMemoryUnit.contains("G")) {
                    limitMemoryValue = limitMemoryValue * 1024;
                }
                totalLimitCpuValue += limitCpuValue;
                totalLimitMemoryValue += limitMemoryValue;
            }
            PodDto.this.totalLimitCpu = (totalLimitCpuValue == 0) ? null : totalLimitCpuValue + " Cores";
            PodDto.this.totalLimitMemory = (totalLimitMemoryValue == 0) ? null : totalLimitMemoryValue + " MiB";
            return this;
        }
    }


    private int getResourceValue(Quantity quantity) {
        if (quantity == null) {
            return 0;
        }
        String amount = quantity.getAmount();
        if (StringUtils.isEmpty(amount)) {
            return 0;
        }
        Pattern p = Pattern.compile(RESOURCE_VALUE_PATTERN);
        Matcher matcher = p.matcher(amount);
        if (matcher.find()) {
            String value = matcher.group();
            return Integer.parseInt(value);
        }
        return 0;
    }

    private String getResourceUnit(Quantity quantity) {
        if (quantity == null) {
            return "";
        }
        String amount = quantity.getAmount();
        if (StringUtils.isEmpty(amount)) {
            return "";
        }
        Pattern p = Pattern.compile(RESOURCE_UNIT_PATTERN);
        Matcher matcher = p.matcher(amount);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";


    }
}
