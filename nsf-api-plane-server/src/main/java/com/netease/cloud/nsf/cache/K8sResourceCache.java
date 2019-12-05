package com.netease.cloud.nsf.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netease.cloud.nsf.cache.meta.PodDTO;
import com.netease.cloud.nsf.cache.meta.WorkLoadDTO;
import com.netease.cloud.nsf.configuration.ApiPlaneConfig;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.RestTemplateClient;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectReference;
import io.fabric8.kubernetes.api.model.apps.DaemonSetList;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    ApiPlaneConfig config;

    @Autowired
    GatewayService gatewayService;

//    @Autowired
//    @Qualifier("originalKubernetesClient")
//    private KubernetesClient kubernetesClient;

    @Autowired
    private MultiClusterK8sClient multiClusterK8sClient;

    private Map<K8sResourceEnum, K8sResourceInformer> resourceInformerMap = new HashMap<com.netease.cloud.nsf.core.k8s.K8sResourceEnum, K8sResourceInformer>();
    private static final Logger log = LoggerFactory.getLogger(K8sResourceCache.class);
    private static final String UPDATE_RESOURCE_DURATION = "0 0/1 * * * ? *";
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

        Map<String, MultiClusterK8sClient.ClientSet> allClients = multiClusterK8sClient.getAllClients();

        ResourceUpdatedListener versionUpdateListener = new VersionUpdateListener();
        // 初始化Deploy informer
        K8sResourceInformer deployInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Deployment)
                .addUpdateListener(versionUpdateListener)
                .addMixedOperation(getDeployMixedOperationList(allClients))
                .addResourceList(getDeployList(allClients))
                .build();
        resourceInformerMap.putIfAbsent(Deployment, deployInformer);

        // 初始化StatefulSet informer
        K8sResourceInformer statefulSetInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(StatefulSet)
                .addMixedOperation(getStatefulSetMixedOperationList(allClients))
                .addUpdateListener(versionUpdateListener)
                .addResourceList(getStsList(allClients))
                .build();
        resourceInformerMap.putIfAbsent(StatefulSet, statefulSetInformer);


        // 初始化Pod informer
        K8sResourceInformer podInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Pod)
                .addMixedOperation(getPodMixedOperationList(allClients))
                .addResourceList(getPodList(allClients))
                .build();
        resourceInformerMap.putIfAbsent(Pod, podInformer);

        // 初始化replicaSet informer
        K8sResourceInformer replicaSetInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(ReplicaSet)
                .addMixedOperation(getReplicasSetMixedOperationList(allClients))
                .addResourceList(getReplicaSet(allClients))
                .build();
        resourceInformerMap.putIfAbsent(ReplicaSet, replicaSetInformer);


        // 初始化Service informer
        K8sResourceInformer serviceInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Service)
                .addMixedOperation(getServiceMixedOperationList(allClients))
                .addResourceList(getServiceList(allClients))
                .build();
        resourceInformerMap.putIfAbsent(Service, serviceInformer);


        // 初始化Endpoint informer
        K8sResourceInformer endpointInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Endpoint)
                .addMixedOperation(getEndPointMixedOperationList(allClients))
                .addResourceList(getEndPointList(allClients))
                .build();
        resourceInformerMap.put(Endpoint, endpointInformer);


    }

    private List<ClusterResourceList> getEndPointList(Map<String, MultiClusterK8sClient.ClientSet> allClients) {
        List<ClusterResourceList> result = new ArrayList<>();
        allClients.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterResourceList(client.originalK8sClient
                        .endpoints()
                        .list(), name));
            }
        });
        return result;
    }

    private List<ClusterResourceList> getServiceList(Map<String, MultiClusterK8sClient.ClientSet> allClients) {
        List<ClusterResourceList> result = new ArrayList<>();
        allClients.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterResourceList(client.originalK8sClient
                        .services()
                        .list(), name));
            }
        });
        return result;
    }

    private List<ClusterResourceList> getReplicaSet(Map<String, MultiClusterK8sClient.ClientSet> allClients) {
        List<ClusterResourceList> result = new ArrayList<>();
        allClients.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterResourceList(client.originalK8sClient
                        .apps()
                        .replicaSets()
                        .list(), name));
            }
        });
        return result;
    }

    private List<ClusterResourceList> getPodList(Map<String, MultiClusterK8sClient.ClientSet> allClients) {
        List<ClusterResourceList> result = new ArrayList<>();
        allClients.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterResourceList(client.originalK8sClient
                        .pods()
                        .list(), name));
            }
        });
        return result;
    }

    private List<ClusterResourceList> getDeployList(Map<String, MultiClusterK8sClient.ClientSet> allClients) {
        List<ClusterResourceList> result = new ArrayList<>();
        allClients.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterResourceList(client.originalK8sClient
                        .apps()
                        .deployments()
                        .list(), name));
            }
        });
        return result;
    }

    private List<ClusterResourceList> getStsList(Map<String, MultiClusterK8sClient.ClientSet> allClients) {
        List<ClusterResourceList> result = new ArrayList<>();
        allClients.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterResourceList(client.originalK8sClient
                        .apps()
                        .statefulSets()
                        .list(), name));
            }
        });
        return result;
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
    public List<WorkLoadDTO<T>> getWorkLoadByServiceInfo(String projectId, String namespace, String serviceName, String clusterId) {
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
                        .map(obj -> new WorkLoadDTO<>(obj, getServiceName(service), clusterId))
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<PodDTO<T>> getPodByWorkLoadInfo(String clusterId, String kind, String namespace, String name) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        T obj = (T) store.get(kind, namespace, name);
        if (obj == null) {
            return new ArrayList<>();
        }
        return (List<PodDTO<T>>) store.listResourceByOwnerReference(Pod.name(), obj)
                .stream()
                .map(po -> new PodDTO<T>((T) po, clusterId))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkLoadDTO<T>> getAllWorkLoad() {
        List<WorkLoadDTO<T>> workLoadList = new ArrayList<>();
        List<String> clusterIdList = ResourceStoreFactory.listClusterId();
        for (String clusterId : clusterIdList) {
            workLoadList.addAll(getAllWorkLoadByClusterId(clusterId));
        }
        return workLoadList;
    }

    @Override
    public List getAllWorkLoadByClusterId(String clusterId) {
        List<WorkLoadDTO<T>> workLoadList = new ArrayList<>();
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        List<T> serviceList = store.listByKind(Service.name());
        serviceList.forEach(service -> workLoadList.addAll(getWorkLoadByIndex(clusterId,
                service.getMetadata().getNamespace(),
                service.getMetadata().getName()).stream()
                .map(obj -> new WorkLoadDTO<>(obj, getServiceName(service), clusterId))
                .collect(Collectors.toList())
        ));
        return workLoadList;
    }

    @Override
    public List getWorkLoadByServiceInfoAllClusterId(String projectId, String namespace, String serviceName) {
        List<WorkLoadDTO<T>> workLoadDtoList = new ArrayList<>();
        List<String> clusterIdList = ResourceStoreFactory.listClusterId();
        for (String clusterId : clusterIdList) {
            workLoadDtoList.addAll(getWorkLoadByServiceInfo(projectId, namespace, serviceName, clusterId));
        }
        return workLoadDtoList;
    }

    @Override
    public T getResource(String clusterId, String kind, String namespace, String name) {
        if (!ResourceStoreFactory.listClusterId().contains(clusterId)) {
            throw new ApiPlaneException("ClusterId not found");
        }
        return (T) ResourceStoreFactory.getResourceStore(clusterId).get(kind, namespace, name);
    }

    @Override
    public List<WorkLoadDTO<T>> getWorkLoadListWithSidecarVersion(List workLoadDTOList) {
        if (CollectionUtils.isEmpty(workLoadDTOList)) {
            return new ArrayList<>();
        }
        workLoadDTOList.forEach(workLoadDTO -> addSidecarVersionOnWorkLoad((WorkLoadDTO<T>) workLoadDTO));
        return workLoadDTOList;
    }

    @Override
    public List<PodDTO<T>> getPodListWithSidecarVersion(List podDTOList) {
        if (CollectionUtils.isEmpty(podDTOList)) {
            return new ArrayList<>();
        }
        podDTOList.forEach(podDTO -> addSidecarVersionOnPod((PodDTO<T>) podDTO));
        return podDTOList;
    }

    private PodDTO<T> addSidecarVersionOnPod(PodDTO<T> podDTO) {
        PodVersion queryVersion = new PodVersion();
        queryVersion.setClusterIP(podDTO.getClusterId());
        queryVersion.setNamespace(podDTO.getNamespace());
        queryVersion.setPodNames(Arrays.asList(podDTO.getName()));
        List<PodStatus> podStatuses = gatewayService.queryByPodNameList(queryVersion);
        if (CollectionUtils.isEmpty(podStatuses)) {
            return podDTO;
        }
        PodStatus status = podStatuses.get(0);
        podDTO.setSidecarStatus(status.getCurrentVersion());
        return podDTO;
    }

    private WorkLoadDTO<T> addSidecarVersionOnWorkLoad(WorkLoadDTO<T> workLoadDTO) {
        List<PodDTO<T>> podByWorkLoadInfo = getPodByWorkLoadInfo(workLoadDTO.getClusterId(), workLoadDTO.getKind(), workLoadDTO.getNamespace(),
                workLoadDTO.getName());
        PodVersion queryVersion = new PodVersion();
        queryVersion.setClusterIP(workLoadDTO.getClusterId());
        queryVersion.setNamespace(workLoadDTO.getNamespace());
        queryVersion.setPodNames(podByWorkLoadInfo
                .stream()
                .map(pod -> pod.getName())
                .collect(Collectors.toList()));
        List<PodStatus> podStatuses = gatewayService.queryByPodNameList(queryVersion);
        if (CollectionUtils.isEmpty(podStatuses)) {
            return workLoadDTO;
        }
        Set<String> versionSet = podStatuses.stream().
                map(podStatus -> podStatus.getCurrentVersion())
                .collect(Collectors.toSet());
        workLoadDTO.setSidecarVersion(new ArrayList<>(versionSet));
        return workLoadDTO;
    }

    private List<T> getWorkLoadByIndex(String clusterId, String namespace, String name) {
        return workLoadByServiceCache.getUnchecked(new WorkLoadIndex(clusterId, namespace, name));
    }

    private List<T> doGetWorkLoadList(String clusterId, String namespace, String serviceName) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        Endpoints endpointsByService = (Endpoints) store.get(Endpoint.name(), namespace, serviceName);
        if (endpointsByService == null) {
            return new ArrayList<>();
        }
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


    private List<MixedOperation> getDeployMixedOperationList(Map<String, MultiClusterK8sClient.ClientSet> cm) {
        List<MixedOperation> result = new ArrayList<>();
        cm.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterMixedOperation(name, (MixedOperation) client.originalK8sClient
                        .apps()
                        .deployments()
                        .inAnyNamespace()));
            }
        });
        return result;
    }

    private List<MixedOperation> getStatefulSetMixedOperationList(Map<String, MultiClusterK8sClient.ClientSet> cm) {
        List<MixedOperation> result = new ArrayList<>();
        cm.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterMixedOperation(name, (MixedOperation) client.originalK8sClient
                        .apps()
                        .statefulSets()
                        .inAnyNamespace()));
            }
        });
        return result;
    }

    private List<MixedOperation> getPodMixedOperationList(Map<String, MultiClusterK8sClient.ClientSet> cm) {
        List<MixedOperation> result = new ArrayList<>();
        cm.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterMixedOperation(name, (MixedOperation) client.originalK8sClient
                        .pods()
                        .inAnyNamespace()));
            }
        });
        return result;
    }

    private List<MixedOperation> getServiceMixedOperationList(Map<String, MultiClusterK8sClient.ClientSet> cm) {
        List<MixedOperation> result = new ArrayList<>();
        cm.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterMixedOperation(name, (MixedOperation) client.originalK8sClient
                        .services()
                        .inAnyNamespace()));
            }
        });
        return result;
    }

    private List<MixedOperation> getEndPointMixedOperationList(Map<String, MultiClusterK8sClient.ClientSet> cm) {
        List<MixedOperation> result = new ArrayList<>();
        cm.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterMixedOperation(name, (MixedOperation) client.originalK8sClient
                        .endpoints()
                        .inAnyNamespace()));
            }
        });
        return result;
    }

    private List<MixedOperation> getReplicasSetMixedOperationList(Map<String, MultiClusterK8sClient.ClientSet> cm) {
        List<MixedOperation> result = new ArrayList<>();
        cm.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterMixedOperation(name, (MixedOperation) client.originalK8sClient
                        .apps()
                        .replicaSets()
                        .inAnyNamespace()));
            }
        });
        return result;
    }

    public class VersionUpdateListener implements ResourceUpdatedListener {

        private static final String CREATE_VERSION_URL = "/api/metadata?Version=2018-11-1&Action=CreateVersionForService";
        //"&ServiceName={serviceName}&ProjectCode={projectId}&ServiceVersion={serviceVersion}&EnvName={envName}";

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
            String serviceName = serviceToUpdate.getMetadata().getName() + "."
                    + serviceToUpdate.getMetadata().getNamespace();

            String projectId = serviceToUpdate.getMetadata().getLabels().get(Const.LABEL_NSF_PROJECT_ID);
            String version = obj.getMetadata().getLabels().get(Const.LABEL_NSF_VERSION);
            String envName = obj.getMetadata().getLabels().get(Const.LABEL_NSF_ENV);
            updateVersion(serviceName, projectId, version, envName);

        }

        private void updateVersion(String serviceName, String projectId, String version, String envName) {
            Map<String, String> requestParam = new HashMap<>(4);
            requestParam.put("serviceName", serviceName);
            requestParam.put("projectCode", projectId);
            requestParam.put("serviceVersion", version);
            requestParam.put("envName", envName);
            String url = restTemplateClient.buildRequestUrlWithParameter(K8sResourceCache.this.config.getNsfMetaUrl()
                            + CREATE_VERSION_URL,
                    requestParam);
            try {
                K8sResourceCache.this.restTemplateClient.getForValue(url
                        , requestParam
                        , Const.GET_METHOD
                        , String.class);
            } catch (ApiPlaneException e) {
                log.warn("create version error {}", e.getMessage());
                return;
            }
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
