package com.netease.cloud.nsf.cache.meta;

import com.netease.cloud.nsf.meta.IptablesConfig;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.Const;
import io.fabric8.kubernetes.api.model.*;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangzihao
 */
public class PodDTO extends K8sResourceDTO<Pod> {

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

    private boolean isInjected;

    private String sidecarImage;

    private int versionManagerCrdStatus;

    private String sidecarContainerStatus;

    private final IptablesConfig iptablesConfig;

    private String sidecarVersion;

    private String clusterId;

    private Map<String, String> syncInfo;

    private String controlPlaneVersion;

    private Map<String, String> labels;

    @Override
    public String getClusterId() {
        return clusterId;
    }

    @Override
    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getSidecarVersion() {
        return sidecarVersion;
    }

    public void setSidecarVersion(String sidecarVersion) {
        this.sidecarVersion = sidecarVersion;
    }

    public String getSidecarContainerStatus() {
        return sidecarContainerStatus;
    }

    public void setSidecarContainerStatus(String sidecarContainerStatus) {
        this.sidecarContainerStatus = sidecarContainerStatus;
    }

    public int getVersionManagerCrdStatus() {
        return versionManagerCrdStatus;
    }

    public void setVersionManagerCrdStatus(int versionManagerCrdStatus) {
        this.versionManagerCrdStatus = versionManagerCrdStatus;
    }

    public boolean isInjected() {
        return isInjected;
    }

    public void setInjected(boolean injected) {
        isInjected = injected;
    }

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

    public String getControlPlaneVersion() {
        return controlPlaneVersion;
    }

    public void setControlPlaneVersion(String controlPlaneVersion) {
        this.controlPlaneVersion = controlPlaneVersion;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public PodDTO(Pod pod, String clusterId) {
        super(pod, clusterId);
        this.hostIp = pod.getStatus().getHostIP();
        this.podIp = pod.getStatus().getPodIP();
        this.status = pod.getStatus().getPhase();
        this.isInjected = isInjected(pod);
        this.clusterId = clusterId;
        this.labels = pod.getMetadata().getLabels();

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
                .setStatusInfo("restartCount", cs.getRestartCount().toString())
                .setCurrState(cs.getState());
            totalRestartCount += cs.getRestartCount();
        });
        if (this.totalLimitCpuValue > 0){
            this.totalLimitCpu = String.format(LIMIT_CPU_FORMAT, this.totalLimitCpuValue);
        }
        if (this.totalLimitMemoryValue > 0){
            this.totalLimitMemory = String.format(LIMIT_MEMORY_FORMAT, this.totalLimitMemoryValue);
        }
        containerInfoList.addAll(containerInfoMap.values());

        if (pod.getMetadata()!=null && pod.getMetadata().getAnnotations()!=null){
            this.sidecarVersion = pod.getMetadata().getAnnotations().get("envoy.io/binaryName");
        }

        sidecarImage = CommonUtil.safelyGet(() ->
            pod.getSpec().getContainers().stream()
                .filter(c -> "istio-proxy".equals(c.getName()))
                .findAny()
                .map(Container::getImage)
                .orElse(null)
        );
        iptablesConfig = CommonUtil.safelyGet(() ->
            IptablesConfig.readFromJson(pod.getMetadata().getAnnotations().get("envoy.io/iptablesDetail"))
        );
    }

    public IptablesConfig getIptablesConfig() {
        return iptablesConfig;
    }

    public String getSidecarImage() {
        return sidecarImage;
    }

    public void setSidecarImage(String sidecarImage) {
        this.sidecarImage = sidecarImage;
    }

    public Map<String, String> getSyncInfo() {
        return syncInfo;
    }

    public void setSyncInfo(Map<String, String> syncInfo) {
        this.syncInfo = syncInfo;
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

        public ContainerInfo setCurrState(ContainerState state) {
            if (state.getRunning()!=null){
                statusInfo.put("Status",Const.CONTAINER_STATUS_RUNNING);
                return this;
            }
            if (state.getWaiting()!=null){
                statusInfo.put("Status",Const.CONTAINER_STATUS_WAITING);
                return this;
            }
            if (state.getTerminated()!=null){
                statusInfo.put("Status",Const.CONTAINER_STATUS_TERMINATED);
                return this;
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

    public static boolean isInjected(Pod pod){
        if (pod.getStatus() == null){
            return false;
        }
        if (pod.getStatus().getContainerStatuses() == null){
            return false;
        }
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
        for (ContainerStatus containerStatus : containerStatuses) {
            if (Const.SIDECAR_CONTAINER.equals(containerStatus.getName())
                    &&containerStatus.getState()!=null
                    &&containerStatus.getState().getRunning()!=null){
                return true;
            }
        }
        return false;
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


