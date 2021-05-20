package com.netease.cloud.nsf.cache;


import com.google.common.base.Strings;
import com.netease.cloud.nsf.cache.meta.PodDTO;
import com.netease.cloud.nsf.cache.meta.WorkLoadDTO;
import com.netease.cloud.nsf.configuration.ext.MeshConfig;
import com.netease.cloud.nsf.util.Const;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
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
    private LinkedBlockingQueue<ResourceUpdateEvent> workloadEvent = new LinkedBlockingQueue<>();
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
    }


    public WorkLoadDTO setSidecarInfo(List<HasMetadata> podList,WorkLoadDTO dto) {
        if (CollectionUtils.isEmpty(podList)) {
            dto.setInMesh(false);
        }
        dto.setInMesh(true);
        HashSet<String> versionSet = new HashSet<>();
        for (Object obj : podList) {
            io.fabric8.kubernetes.api.model.Pod pod = (io.fabric8.kubernetes.api.model.Pod) obj;
            if (!PodDTO.isInjected(pod)) {
                dto.setInMesh(false);
            }
            String sidecarVersion = binaryName(pod);
            if (!StringUtils.isEmpty(sidecarVersion)){
                versionSet.add(binaryName(pod));
            }
        }
        dto.setSidecarVersion(new ArrayList<>(versionSet));
        return dto;

    }

    private String binaryName(io.fabric8.kubernetes.api.model.Pod pod){
        if (pod.getMetadata() == null || pod.getMetadata().getAnnotations() == null){
            return null;
        }
        return pod.getMetadata().getAnnotations().get("envoy.io/binaryName");
    }

    private WorkLoadDTO newWorkLoadDTO(String clusterId, HasMetadata obj) {
        String serviceName = getServiceNameByWorkload(obj);
        if (StringUtils.isEmpty(serviceName)) {
            return null;
        }
        String mark = getPodLabelFromWorkload(obj, meshConfig.getTrafficMarkLabel());
        return new WorkLoadDTO(obj, serviceName, mark, clusterId);
    }

    public String getServiceNameByWorkload(HasMetadata resourceObject) {
        String namespace = resourceObject.getMetadata().getNamespace();
        String appName = null;
        if (resourceObject.getMetadata().getLabels() != null) {
            appName = Strings.emptyToNull(resourceObject.getMetadata().getLabels().get(meshConfig.getSelectorAppKey()));
        }
        String label = meshConfig.getSelectorAppKey();
        if (appName == null) {
            appName = getPodLabelFromWorkload(resourceObject, label);
        }
        if (appName == null) {
            log.debug("no app info for workload [{}] ", resourceObject.getMetadata().getName());
            return null;
        }
        return appName + Const.SEPARATOR_DOT + namespace;
    }

    private String getPodLabelFromWorkload(HasMetadata resourceObject, String label) {
        if (resourceObject instanceof io.fabric8.kubernetes.api.model.apps.Deployment) {
            io.fabric8.kubernetes.api.model.apps.Deployment deployment = (io.fabric8.kubernetes.api.model.apps.Deployment) resourceObject;
            Map<String, String> labels = deployment.getSpec().getTemplate().getMetadata().getLabels();
            if (labels != null && !labels.isEmpty()) {
                return Strings.emptyToNull(labels.get(label));
            }
        } else if (resourceObject instanceof io.fabric8.kubernetes.api.model.apps.StatefulSet) {
            io.fabric8.kubernetes.api.model.apps.StatefulSet statefulSet = (io.fabric8.kubernetes.api.model.apps.StatefulSet) resourceObject;
            Map<String, String> labels = statefulSet.getSpec().getTemplate().getMetadata().getLabels();
            if (labels != null && !labels.isEmpty()) {
                return Strings.emptyToNull(labels.get(label));
            }
        } else {
            log.warn("unknown resource type for workload [{}] ", resourceObject.getMetadata().getName());
        }
        return null;
    }

    private void syncAll(List resourceList, String clusterId) {
        if (CollectionUtils.isEmpty(resourceList)) {
            return;
        }
        Map<String, List<WorkLoadDTO>> newAppWorkLoadMap = new HashMap<>();
        for (Object obj : resourceList) {
            WorkLoadDTO newWorkLoadDTO = newWorkLoadDTO(clusterId, (HasMetadata) obj);
            if (newWorkLoadDTO == null) {
                continue;
            }
            List<WorkLoadDTO> workloadByServiceName = newAppWorkLoadMap.computeIfAbsent(newWorkLoadDTO.getServiceName(), k -> new ArrayList<>());
            workloadByServiceName.add(newWorkLoadDTO);
        }
        appWorkLoadMap.put(clusterId,newAppWorkLoadMap);
    }

    public List<WorkLoadDTO> getWorkloadListByApp(String clusterId,String key) {
        return this.appWorkLoadMap.getOrDefault(clusterId, new HashMap<>()).getOrDefault(key,new ArrayList<>());
    }

    private void removeWorkloadList(HasMetadata obj, String clusterId) {
        WorkLoadDTO removedWorkLoad = newWorkLoadDTO(clusterId, obj);
        if (removedWorkLoad == null) {
            return;
        }
        Map<String, List<WorkLoadDTO>> workloadByAppName = this.appWorkLoadMap.computeIfAbsent(clusterId, k -> new HashMap<>());
        List<WorkLoadDTO> oldWorkloadList = workloadByAppName.computeIfAbsent(removedWorkLoad.getServiceName(), k -> new ArrayList<>());
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
            workloadByAppName.remove(removedWorkLoad.getServiceName());
        } else {
            workloadByAppName.put(removedWorkLoad.getServiceName(), newWorkloadList);
        }
    }

    private void updateWorkloadList(HasMetadata obj, String clusterId) {
        WorkLoadDTO newWorkLoadDTO = newWorkLoadDTO(clusterId, obj);
        if (newWorkLoadDTO == null) {
            return;
        }
        Map<String, List<WorkLoadDTO>> workloadByAppName = this.appWorkLoadMap.computeIfAbsent(clusterId, k -> new HashMap<>());
        List<WorkLoadDTO> oldWorkloadList = workloadByAppName.computeIfAbsent(newWorkLoadDTO.getServiceName(), k -> new ArrayList<>());
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
            workloadByAppName.remove(newWorkLoadDTO.getServiceName());
        } else {
            workloadByAppName.put(newWorkLoadDTO.getServiceName(), newWorkloadList);
        }
    }

    private void addWorkloadList(HasMetadata obj, String clusterId) {
        WorkLoadDTO newWorkLoadDTO = newWorkLoadDTO(clusterId, obj);
        if (newWorkLoadDTO == null) {
            return;
        }
        Map<String, List<WorkLoadDTO>> workloadByAppName = this.appWorkLoadMap.computeIfAbsent(clusterId, k -> new HashMap<>());
        List<WorkLoadDTO> oldWorkloadList = workloadByAppName.computeIfAbsent(newWorkLoadDTO.getServiceName(), k -> new ArrayList<>());
        oldWorkloadList.add(newWorkLoadDTO);
        workloadByAppName.put(newWorkLoadDTO.getServiceName(), oldWorkloadList);
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
