package com.netease.cloud.nsf.cache;


import com.netease.cloud.nsf.cache.meta.PodDTO;
import com.netease.cloud.nsf.cache.meta.WorkLoadDTO;
import com.netease.cloud.nsf.configuration.ext.MeshConfig;
import com.netease.cloud.nsf.util.Const;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import me.snowdrop.istio.api.networking.v1alpha3.PodVersionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.netease.cloud.nsf.core.k8s.K8sResourceEnum.*;

/**
 * @author zhangzihao
 */
@Component
public class ResourceCacheManager implements ResourceEventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ResourceCacheManager.class);


    @Autowired
    ResourceCache resourceCache;

    @Autowired
    MeshConfig meshConfig;

    private static final String UPDATE_EVENT = "MODIFIED";
    private static final String CREATE_EVENT = "ADDED";
    private static final String DELETE_EVENT = "DELETED";
    private static final String SYNC_EVENT = "SYNC";
    private Map<String, AtomicLong> lastResourceVersion = new ConcurrentHashMap<>();
    private Map<String,Map<String, List<WorkLoadDTO>>> appWorkLoadMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, String>> versionManagerMap = new ConcurrentHashMap<>();
    private LinkedBlockingQueue<ResourceUpdateEvent> workloadEvent = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<ResourceUpdateEvent> versionManagerEvent = new LinkedBlockingQueue<>();
    private ExecutorService eventProcessor = Executors.newCachedThreadPool();


    @EventListener(ApplicationReadyEvent.class)
    private void startProcessor() {
        eventProcessor.execute(() -> {
            log.info("start to process workload update event");
            for (; ; ) {
                try {
                    ResourceUpdateEvent updateEvent = null;
                    try {
                        updateEvent = workloadEvent.take();
                    } catch (InterruptedException e) {
                        log.warn("get update event from blocking queue error");
                    }

                    String eventType = updateEvent.getEventType();
                    String clusterId = updateEvent.getClusterId();
                    Long resourceVersion = updateEvent.getResourceVersion();
                    if (resourceVersion < getCurrentResourceVersion(clusterId)) {
                        log.info("ignore event with stale resourceVersion [{}]", resourceVersion);
                        continue;
                    }
                    switch (eventType) {
                        case UPDATE_EVENT:
                            updateWorkloadList(updateEvent.getResourceObject(), clusterId);
                            break;
                        case CREATE_EVENT:
                            addWorkloadList(updateEvent.getResourceObject(), clusterId);
                            break;
                        case DELETE_EVENT:
                            removeWorkloadList(updateEvent.getResourceObject(), clusterId);
                            break;
                        case SYNC_EVENT:
                            syncAll(updateEvent.getResourceList(), clusterId);
                            continue;
                        default:

                    }
                } catch (Exception e) {
                    log.error("process workload update event error",e);
                    continue;
                }
            }
        });
//       eventProcessor.execute(() -> {
//            log.info("start to process version manager update event");
//            for (; ; ) {
//                try {
//                    ResourceUpdateEvent updateEvent = null;
//                    try {
//                        updateEvent = versionManagerEvent.take();
//                    } catch (InterruptedException e) {
//                        log.warn("get update event from blocking queue error");
//                    }
//                    String clusterId = updateEvent.getClusterId();
//                    String namespace = updateEvent.getNamespace();
//                    String key = clusterId + Const.SEPARATOR_DOT + namespace;
//                    String eventType = updateEvent.getEventType();
//                    switch (eventType) {
//                        case DELETE_EVENT:
//                            versionManagerMap.remove(key);
//                            break;
//                        case SYNC_EVENT:
//                            updateAllVersionInfo(updateEvent.getResourceList(), clusterId);
//                            break;
//                        default:
//                            updateVersionInfoForKey(key, (me.snowdrop.istio.api.networking.v1alpha3.VersionManager) updateEvent.getResourceObject());
//                    }
//                } catch (Exception e) {
//                    log.error("process version manager update event error",e);
//                    continue;
//                }
//            }
//        });
    }

    private void updateAllVersionInfo(List resourceList, String clusterId) {
        if (resourceList == null) {
            return;
        }

        for (Object resource : resourceList) {
            me.snowdrop.istio.api.networking.v1alpha3.VersionManager versionManager = (me.snowdrop.istio.api.networking.v1alpha3.VersionManager) resource;
            String namespace = versionManager.getMetadata().getNamespace();
            updateVersionInfoForKey(clusterId + Const.SEPARATOR_DOT + namespace, versionManager);
        }

    }

    private void updateVersionInfoForKey(String key, me.snowdrop.istio.api.networking.v1alpha3.VersionManager versionManager) {
        if (versionManager ==null || versionManager.getSpec() == null || versionManager.getSpec().getStatus() == null){
            versionManagerMap.remove(key);
            return;
        }
        List<PodVersionStatus> podVersionStatus = versionManager.getSpec().getStatus().getPodVersionStatus();
        if (CollectionUtils.isEmpty(podVersionStatus)) {
            versionManagerMap.remove(key);
        } else {
            Map<String, String> sidecarVersionForPod = new HashMap<>();
            for (PodVersionStatus versionStatus : podVersionStatus) {
                String podName = versionStatus.getPodName();
                sidecarVersionForPod.put(podName, versionStatus.getCurrentVersion());
            }
            versionManagerMap.put(key, sidecarVersionForPod);
        }

    }

    private String getSidecarVersionOnPod(String clusterId, String namespace, String name) {
        Map<String, String> sidecarVersionForPod = versionManagerMap.get(clusterId + Const.SEPARATOR_DOT + namespace);
        if (sidecarVersionForPod == null || sidecarVersionForPod.isEmpty()) {
            return null;
        }
        return sidecarVersionForPod.get(name);
    }

    public boolean isInjectedWorkload(String clusterId, String kind, String namespace, String name) {
        List podInfoByWorkLoadInfo = resourceCache.getPodInfoByWorkLoadInfo(clusterId, kind, namespace, name);
        if (CollectionUtils.isEmpty(podInfoByWorkLoadInfo)) {
            return false;
        }
        for (Object obj : podInfoByWorkLoadInfo) {
            io.fabric8.kubernetes.api.model.Pod pod = (io.fabric8.kubernetes.api.model.Pod) obj;
            if (!PodDTO.isInjected(pod)) {
                return false;
            }
        }
        return true;
    }

    public String getServiceNameByWorkload(HasMetadata resourceObject) {
        String namespace = resourceObject.getMetadata().getNamespace();
        String appName = null;
        if (resourceObject.getMetadata().getLabels() != null) {
            appName = resourceObject.getMetadata().getLabels().get(meshConfig.getSelectorAppKey());
        }
        if (StringUtils.isEmpty(appName)) {
            if (resourceObject instanceof io.fabric8.kubernetes.api.model.apps.Deployment) {
                io.fabric8.kubernetes.api.model.apps.Deployment deployment = (io.fabric8.kubernetes.api.model.apps.Deployment) resourceObject;
                Map<String, String> label = deployment.getSpec().getTemplate().getMetadata().getLabels();
                if (label != null && !label.isEmpty()) {
                    appName = label.get(meshConfig.getSelectorAppKey());
                }
            } else if (resourceObject instanceof io.fabric8.kubernetes.api.model.apps.StatefulSet) {
                io.fabric8.kubernetes.api.model.apps.StatefulSet statefulSet = (io.fabric8.kubernetes.api.model.apps.StatefulSet) resourceObject;
                Map<String, String> label = statefulSet.getSpec().getTemplate().getMetadata().getLabels();
                if (label != null && !label.isEmpty()) {
                    appName = label.get(meshConfig.getSelectorAppKey());
                }
            } else {
                log.warn("unknown resource type for workload [{}] ", resourceObject.getMetadata().getName());
                return null;
            }
        }
        if (StringUtils.isEmpty(appName)) {
            log.warn("no app info for workload [{}] ", resourceObject.getMetadata().getName());
            return null;
        }
        return appName + Const.SEPARATOR_DOT + namespace;
    }

    private void syncAll(List resourceList, String clusterId) {
        if (CollectionUtils.isEmpty(resourceList)) {
            return;
        }
        Map<String, List<WorkLoadDTO>> newAppWorkLoadMap = new HashMap<>();
        for (Object obj : resourceList) {
            HasMetadata workload = (HasMetadata) obj;
            String serviceName = getServiceNameByWorkload(workload);
            if (StringUtils.isEmpty(serviceName)) {
                continue;
            }
            WorkLoadDTO newWorkLoadDTO = new WorkLoadDTO(workload, serviceName, clusterId);
            List<WorkLoadDTO> workloadByServiceName = newAppWorkLoadMap.computeIfAbsent(serviceName, k -> new ArrayList<>());
            workloadByServiceName.add(newWorkLoadDTO);
        }
        appWorkLoadMap.put(clusterId,newAppWorkLoadMap);
    }

    public List<WorkLoadDTO> getWorkloadListByServiceName(String clusterId,String key) {
        return this.appWorkLoadMap.getOrDefault(clusterId, new HashMap<>()).getOrDefault(key,new ArrayList<>());
    }

    private void removeWorkloadList(HasMetadata obj, String clusterId) {
        String serviceName = getServiceNameByWorkload(obj);
        if (StringUtils.isEmpty(serviceName)) {
            return;
        }
        Map<String, List<WorkLoadDTO>> workloadByAppName = this.appWorkLoadMap.computeIfAbsent(clusterId, k -> new HashMap<>());
        List<WorkLoadDTO> oldWorkloadList = workloadByAppName.computeIfAbsent(serviceName, k -> new ArrayList<>());
        WorkLoadDTO removedWorkLoad = new WorkLoadDTO(obj, serviceName, clusterId);
        List<WorkLoadDTO> newWorkloadList = new ArrayList<>();
        for (WorkLoadDTO loadDTO : oldWorkloadList) {
            if (!loadDTO.getKind().equals(removedWorkLoad.getKind()) ||
                    !loadDTO.getNamespace().equals(removedWorkLoad.getNamespace()) ||
                    !loadDTO.getName().equals(removedWorkLoad.getName()) ||
                    !loadDTO.getClusterId().equals(removedWorkLoad.getClusterId())) {
                newWorkloadList.add(loadDTO);
            }
        }
        if (CollectionUtils.isEmpty(newWorkloadList)) {
            workloadByAppName.remove(serviceName);
        } else {
            workloadByAppName.put(serviceName, newWorkloadList);
        }
    }

    private void updateWorkloadList(HasMetadata obj, String clusterId) {
        String serviceName = getServiceNameByWorkload(obj);
        if (StringUtils.isEmpty(serviceName)) {
            return;
        }
        Map<String, List<WorkLoadDTO>> workloadByAppName = this.appWorkLoadMap.computeIfAbsent(clusterId, k -> new HashMap<>());
        List<WorkLoadDTO> oldWorkloadList = workloadByAppName.computeIfAbsent(serviceName, k -> new ArrayList<>());
        WorkLoadDTO newWorkLoadDTO = new WorkLoadDTO(obj, serviceName, clusterId);
        List<WorkLoadDTO> newWorkloadList = new ArrayList<>();
        for (WorkLoadDTO loadDTO : oldWorkloadList) {
            if (!loadDTO.getKind().equals(newWorkLoadDTO.getKind()) ||
                    !loadDTO.getNamespace().equals(newWorkLoadDTO.getNamespace()) ||
                    !loadDTO.getName().equals(newWorkLoadDTO.getName()) ||
                    !loadDTO.getClusterId().equals(newWorkLoadDTO.getClusterId())) {
                newWorkloadList.add(loadDTO);
            }
        }
        newWorkloadList.add(newWorkLoadDTO);
        if (CollectionUtils.isEmpty(newWorkloadList)) {
            workloadByAppName.remove(serviceName);
        } else {
            workloadByAppName.put(serviceName, newWorkloadList);
        }
    }

    private void addWorkloadList(HasMetadata obj, String clusterId) {
        String serviceName = getServiceNameByWorkload(obj);
        if (StringUtils.isEmpty(serviceName)) {
            return;
        }
        Map<String, List<WorkLoadDTO>> workloadByAppName = this.appWorkLoadMap.computeIfAbsent(clusterId, k -> new HashMap<>());
        List<WorkLoadDTO> oldWorkloadList = workloadByAppName.computeIfAbsent(serviceName, k -> new ArrayList<>());
        WorkLoadDTO newWorkLoadDTO = new WorkLoadDTO(obj, serviceName, clusterId);
        oldWorkloadList.add(newWorkLoadDTO);
        workloadByAppName.put(serviceName, oldWorkloadList);
    }

    @Override
    public void dispatch(ResourceUpdateEvent event) {

        if (isWorkloadEvent(event)) {
            workloadEvent.offer(event);
        }

    }

    private boolean isVersionManagerEvent(ResourceUpdateEvent event) {
        if (event.getKind() == null) {
            return false;
        }
        return event.getKind().equals(VersionManager.name());
    }

    private boolean isWorkloadEvent(ResourceUpdateEvent event) {
        if (event.getKind() == null) {
            return false;
        }
        return event.getKind().equals(Deployment.name())
                || event.getKind().equals(StatefulSet.name());
    }


    private long getCurrentResourceVersion(String clusterId) {
        return lastResourceVersion.computeIfAbsent(clusterId, c -> new AtomicLong()).get();
    }

}
