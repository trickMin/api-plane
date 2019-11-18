package com.netease.cloud.nsf.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netease.cloud.nsf.cache.meta.PodDto;
import com.netease.cloud.nsf.cache.meta.WorkLoadDto;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.RestTemplateClient;
import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.k8s.K8sResourceEnum.*;


/**
 * @author zhangzihao
 */
@Service
public class K8sResourceCache<T extends HasMetadata> implements ResourceCache {

    @Autowired
    private RestTemplateClient restTemplateClient;

    @Autowired
    private KubernetesClient kubernetesClient;

    private Map<K8sResourceEnum, K8sResourceInformer> resourceInformerMap = new HashMap<com.netease.cloud.nsf.core.k8s.K8sResourceEnum, K8sResourceInformer>();
    private static final Logger log = LoggerFactory.getLogger(K8sResourceCache.class);
    private static final String UPDATE_RESOURCE_DURATION = "0 0/1 * * * ?";
    private static int WORK_LOAD_CACHE_MAX_SIZE = 100;
    private static int WORK_LOAD_CACHE_REFRESH_DURATION = 5;
    private LoadingCache<WorkLoadIndex, List<T>> workLoadByServiceCache = CacheBuilder.newBuilder()
            .maximumSize(WORK_LOAD_CACHE_MAX_SIZE)
            .refreshAfterWrite(WORK_LOAD_CACHE_REFRESH_DURATION, TimeUnit.SECONDS)
            .build(new CacheLoader<WorkLoadIndex, List<T>>() {
                @Override
                public List<T> load(WorkLoadIndex index) {
                    return doGetWorkLoadList(index.getClusterId(), index.getNamespace(), index.getName());
                }

            });


    @PostConstruct
    public void initInformer() {

        ResourceUpdatedListener versionUpdateListener = new VersionUpdateListener();
        // 初始化Deploy informer
        K8sResourceInformer deployInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Deployment)
                .addUpdateListener(versionUpdateListener)
                .addMixedOperation(kubernetesClient.apps().deployments())
                .build();
        resourceInformerMap.putIfAbsent(Deployment, deployInformer);

        // 初始化StatefulSet informer
        K8sResourceInformer statefulSetInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(StatefulSet)
                .addMixedOperation(kubernetesClient.apps().statefulSets())
                .addUpdateListener(versionUpdateListener)
                .build();
        resourceInformerMap.putIfAbsent(StatefulSet, statefulSetInformer);


        // 初始化Pod informer
        K8sResourceInformer podInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Pod)
                .addMixedOperation(kubernetesClient.pods())
                .build();
        resourceInformerMap.putIfAbsent(Pod, podInformer);

        // 初始化replicaSet informer
        K8sResourceInformer replicaSetInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(ReplicaSet)
                .addMixedOperation(kubernetesClient.apps().replicaSets())
                .build();
        resourceInformerMap.putIfAbsent(ReplicaSet, replicaSetInformer);


        // 初始化Service informer
        K8sResourceInformer serviceInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Service)
                .addMixedOperation(kubernetesClient.services())
                .build();
        resourceInformerMap.putIfAbsent(Service, serviceInformer);


        // 初始化Endpoint informer
        K8sResourceInformer endpointInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Endpoint)
                .addMixedOperation(kubernetesClient.endpoints())
                .build();
        resourceInformerMap.put(Endpoint, endpointInformer);


    }


    @EventListener(ApplicationReadyEvent.class)
    public void startInformer() {
        resourceInformerMap.values().forEach(K8sResourceInformer::start);
    }

    @Scheduled(cron = UPDATE_RESOURCE_DURATION)
    public void updateResource() {
        resourceInformerMap.values().forEach(K8sResourceInformer::replaceResource);
    }

    @Override
    public List<WorkLoadDto<T>> getWorkLoadByServiceInfo(String projectId, String namespace, String serviceName, String clusterId) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        List<T> serviceList = store.listByKind(Service.name());
        for (T service : serviceList) {
            if (service.getMetadata().getLabels() == null || service.getMetadata().getLabels().isEmpty()) {
                continue;
            }
            if (service.getMetadata().getLabels().get(Const.LABEL_NSF_PROJECT_ID) != null &&
                    service.getMetadata().getLabels().get(Const.LABEL_NSF_PROJECT_ID).equals(projectId) &&
                    service.getMetadata().getName().equals(serviceName) &&
                    service.getMetadata().getNamespace().equals(namespace)) {
                return getWorkLoadByIndex(clusterId,
                        service.getMetadata().getNamespace(),
                        service.getMetadata().getName()).stream()
                        .map(obj -> new WorkLoadDto<>(obj, getServiceName(service), clusterId))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<PodDto<T>> getPodByWorkLoadInfo(String clusterId, String kind, String namespace, String name) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        T obj = (T) store.get(kind, namespace, name);
        if (obj == null) {
            return new ArrayList<>();
        }
        return (List<PodDto<T>>) store.listResourceByOwnerReference(Pod.name(), obj)
                .stream()
                .map(po -> new PodDto<T>((T) po, clusterId))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkLoadDto<T>> getAllWorkLoad() {
        List<WorkLoadDto<T>> workLoadList = new ArrayList<>();
        Map<String, OwnerReferenceSupportStore> resourceStoreMap = ResourceStoreFactory.getResourceStoreMap();
        for (Map.Entry<String, OwnerReferenceSupportStore> keyValue : resourceStoreMap.entrySet()) {
            String clusterId = keyValue.getKey();
            OwnerReferenceSupportStore store = keyValue.getValue();
            List<T> serviceList = store.listByKind(Service.name());
            serviceList.forEach(service -> workLoadList.addAll(getWorkLoadByIndex(clusterId,
                    service.getMetadata().getNamespace(),
                    service.getMetadata().getName()).stream()
                    .map(obj -> new WorkLoadDto<>(obj, getServiceName(service), clusterId))
                    .collect(Collectors.toList())
            ));
        }
        return workLoadList;
    }

    private List<T> getWorkLoadByIndex(String clusterId, String namespace, String name) {
        return workLoadByServiceCache.getUnchecked(new WorkLoadIndex(clusterId, namespace, name));
    }

    private List<T> doGetWorkLoadList(String clusterId, String namespace, String serviceName) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        Endpoints endpointsByService = (Endpoints) store.get(Endpoint.name(), namespace, serviceName);
        //从endpoints信息中解析出关联的pod列表
        List<ObjectReference> podReferences = endpointsByService.getSubsets()
                .stream()
                .flatMap(sub -> sub.getAddresses().stream())
                .map(EndpointAddress::getTargetRef)
                .filter(Objects::nonNull)
                .filter(ref -> Pod.name().equals(ref.getKind()))
                .collect(Collectors.toList());

        Set<T> workLoadList = new HashSet<>();
        // 通过pod信息查询全部的负载资源
        if (!CollectionUtils.isEmpty(podReferences)) {
            podReferences.forEach(podRef -> {
                T pod = (T) store.get(Pod.name(), podRef.getNamespace(), podRef.getName());
                workLoadList.addAll(store.listLoadByPod(pod));
            });
        }
        return new ArrayList<>(workLoadList);
    }

    private String getServiceName(T obj) {
        if (obj instanceof io.fabric8.kubernetes.api.model.Service) {
            return obj.getMetadata().getName() + "." + obj.getMetadata().getNamespace();
        } else {
            throw new IllegalArgumentException("resource type must be service");
        }
    }


    public class VersionUpdateListener implements ResourceUpdatedListener {
        private static final String CREATE_VERSION_URL = "api/metadata?Version=2018-11-1&Action=CreateVersionForService" +
                "ServiceName={serviceName}&ProjectId={projectId}&ServiceVersion={serviceVersion}";

        @Override
        public void notify(ResourceUpdateEvent e) {
            String clusterId = e.getClusterId();
            OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
            List<T> currentService = (List<T>) store.listByKindAndNamespace(Service.name(), e.getNamespace());
            T obj = (T) e.getResourceObject();
            T serviceToUpdate = null;
            for (T service : currentService) {
                List<T> workLoadList = K8sResourceCache.this.getWorkLoadByIndex(clusterId,
                        e.getNamespace(),
                        service.getMetadata().getName());
                for (T load : workLoadList) {
                    if (isIdentical(obj, load)) {
                        serviceToUpdate = service;
                        break;
                    }
                }
            }
            if (serviceToUpdate == null) {
                log.info("no service to update version");
                return;
            }
            String serviceName = serviceToUpdate.getMetadata().getNamespace() + "."
                    + serviceToUpdate.getMetadata().getName();

            String projectId = serviceToUpdate.getMetadata().getLabels().get(Const.LABEL_NSF_PROJECT_ID);
            String version = obj.getMetadata().getLabels().get(Const.LABEL_NSF_VERSION);
            updateVersion(serviceName, projectId, version);

        }

        private void updateVersion(String serviceName, String projectId, String version) {
            Map<String, Object> requestParam = new HashMap<>(4);
            requestParam.put("serviceName", serviceName);
            requestParam.put("projectId", projectId);
            requestParam.put("serviceVersion", version);
            K8sResourceCache.this.restTemplateClient.getForValue(CREATE_VERSION_URL, requestParam, Const.POST_METHOD, String.class);
            log.info("create version [{}] for service [{}] in projectId [{}]", version, serviceName, projectId);
        }


        private boolean isIdentical(T obj1, T obj2) {
            return obj1.getKind().equals(obj2.getKind()) &&
                    obj1.getMetadata().getNamespace().equals(obj2.getMetadata().getNamespace()) &&
                    obj1.getMetadata().getName().equals(obj2.getMetadata().getName());
        }

    }

    public class WorkLoadIndex {
        private String clusterId;
        private String namespace;
        private String name;

        public WorkLoadIndex(String clusterId, String namespace, String name) {
            this.clusterId = clusterId;
            this.namespace = namespace;
            this.name = name;
        }

        public String getClusterId() {
            return clusterId;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            WorkLoadIndex that = (WorkLoadIndex) o;
            return Objects.equals(clusterId, that.clusterId) &&
                    Objects.equals(namespace, that.namespace) &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clusterId, namespace, name);
        }
    }


}
