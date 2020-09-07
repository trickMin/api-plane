package com.netease.cloud.nsf.cache;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netease.cloud.nsf.cache.extractor.ResourceExtractorManager;
import com.netease.cloud.nsf.cache.meta.PodDTO;
import com.netease.cloud.nsf.cache.meta.ServiceDto;
import com.netease.cloud.nsf.cache.meta.WorkLoadDTO;
import com.netease.cloud.nsf.configuration.ext.ApiPlaneConfig;
import com.netease.cloud.nsf.configuration.ext.MeshConfig;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.istio.PilotHttpClient;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.service.ServiceMeshService;
import com.netease.cloud.nsf.service.VersionManagerService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.RestTemplateClient;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.k8s.K8sResourceEnum.*;


/**
 * @author zhangzihao
 */
public class K8sResourceCache<T extends HasMetadata> implements ResourceCache {

    @Autowired
    private RestTemplateClient restTemplateClient;

    @Autowired
    private ResourceCacheManager resourceCacheManager;

    @Autowired
    ApiPlaneConfig config;

    @Autowired
    VersionManagerService versionManagerService;

    @Autowired
    ServiceMeshService serviceMeshService;

    @Autowired
    private MeshConfig meshConfig;

    @Autowired
    private MultiClusterK8sClient multiClusterK8sClient;

    @Autowired
    private ResourceExtractorManager extractor;

    @Autowired
    private PilotHttpClient pilotHttpClient;

    private Map<K8sResourceEnum, K8sResourceInformer> resourceInformerMap = new HashMap<com.netease.cloud.nsf.core.k8s.K8sResourceEnum, K8sResourceInformer>();
    private static final Logger log = LoggerFactory.getLogger(K8sResourceCache.class);
    private static final String UPDATE_RESOURCE_DURATION = "0 0/1 * * * *";
    private static int WORK_LOAD_CACHE_MAX_SIZE = 100;
    private static int WORK_LOAD_CACHE_REFRESH_DURATION = 20;
    private LoadingCache<WorkLoadIndex, List<T>> workLoadByServiceCache = CacheBuilder.newBuilder()
            .maximumSize(WORK_LOAD_CACHE_MAX_SIZE)
            .expireAfterWrite(WORK_LOAD_CACHE_REFRESH_DURATION, TimeUnit.SECONDS)
            .build(new CacheLoader<WorkLoadIndex, List<T>>() {
                @Override
                public List<T> load(WorkLoadIndex index) {
                    return doGetWorkLoadListByApp(index.getClusterId(), index.getNamespace(), index.getName());
                }

            });
    private LoadingCache<SelectorIndex, List<T>> workLoadBySelectorCache = CacheBuilder.newBuilder()
            .maximumSize(WORK_LOAD_CACHE_MAX_SIZE)
            .expireAfterWrite(WORK_LOAD_CACHE_REFRESH_DURATION, TimeUnit.SECONDS)
            .build(new CacheLoader<SelectorIndex, List<T>>() {
                @Override
                public List<T> load(SelectorIndex index) {
                    return doGetWorkLoadListBySelector(index.getClusterId(), index.getNamespace(), index.getSelectorLabels());
                }

            });


    @PostConstruct
    public void initInformer() {

        Map<String, MultiClusterK8sClient.ClientSet> allClients = multiClusterK8sClient.getAllClients();

        //ResourceUpdatedListener versionUpdateListener = new VersionUpdateListener();
        // 初始化Deploy informer
        K8sResourceInformer deployInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Deployment)
                .addMixedOperation(getDeployMixedOperationList(allClients))
                .addHttpK8sClient(multiClusterK8sClient)
                .addEventDispatcher(resourceCacheManager)
                .build();
        resourceInformerMap.putIfAbsent(Deployment, deployInformer);

        // 初始化StatefulSet informer
        K8sResourceInformer statefulSetInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(StatefulSet)
                .addMixedOperation(getStatefulSetMixedOperationList(allClients))
                .addHttpK8sClient(multiClusterK8sClient)
                .build();
        resourceInformerMap.putIfAbsent(StatefulSet, statefulSetInformer);


        // 初始化Pod informer
        K8sResourceInformer podInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Pod)
                .addMixedOperation(getPodMixedOperationList(allClients))
                .addHttpK8sClient(multiClusterK8sClient)
                .build();
        resourceInformerMap.putIfAbsent(Pod, podInformer);

        // 初始化replicaSet informer
        K8sResourceInformer replicaSetInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(ReplicaSet)
                .addMixedOperation(getReplicasSetMixedOperationList(allClients))
                .addHttpK8sClient(multiClusterK8sClient)
                .build();
        resourceInformerMap.putIfAbsent(ReplicaSet, replicaSetInformer);


        // 初始化Service informer
        K8sResourceInformer serviceInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Service)
                .addMixedOperation(getServiceMixedOperationList(allClients))
                .addHttpK8sClient(multiClusterK8sClient)
                .build();
        resourceInformerMap.putIfAbsent(Service, serviceInformer);


        // 初始化Endpoint informer
        K8sResourceInformer endpointInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(Endpoints)
                .addMixedOperation(getEndPointMixedOperationList(allClients))
                .addHttpK8sClient(multiClusterK8sClient)
                .build();
        resourceInformerMap.put(Endpoints, endpointInformer);

        K8sResourceInformer daemonSetInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(DaemonSet)
                .addMixedOperation(getDaemonSetMixedOperationList(allClients))
                .addHttpK8sClient(multiClusterK8sClient)
                .build();
        resourceInformerMap.put(DaemonSet, daemonSetInformer);

        CustomResourceDefinition mupCrd = null;
        CustomResourceDefinition vmCrd = null;
        try {
            mupCrd = allClients.get("default").originalK8sClient.customResourceDefinitions().withName("mixerurlpatterns.networking.istio.io").get();
            vmCrd = allClients.get("default").originalK8sClient.customResourceDefinitions().withName("versionmanagers.networking.istio.io").get();
        } catch (Exception ignored) {
            log.error("get crd definition error", ignored);
        }

        if (mupCrd != null) {
            K8sResourceInformer<HasMetadata> mupInformer = new K8sResourceInformer
                .Builder()
                .addResourceKind(MixerUrlPattern)
                .addMixedOperation(getMixerUrlPatternMixedOperationList(allClients, mupCrd))
                .addHttpK8sClient(multiClusterK8sClient)
                .build();

            resourceInformerMap.put(MixerUrlPattern, mupInformer);
        } else {
            log.warn("CRD mixer url pattern not found.");
        }

        if (vmCrd != null){
            K8sResourceInformer<HasMetadata> mupInformer = new K8sResourceInformer
                    .Builder()
                    .addResourceKind(VersionManager)
                    .addMixedOperation(getMixerVersionManagerOperationList(allClients, vmCrd))
                    .addHttpK8sClient(multiClusterK8sClient)
                    .build();

            resourceInformerMap.put(VersionManager, mupInformer);
        }

    }



    @EventListener(ApplicationReadyEvent.class)
    public void startInformer() {
        if (config.getStartInformer().equals(Const.OPTION_FALSE)) {
            return;
        }
        resourceInformerMap.values().forEach(K8sResourceInformer::start);
    }

    @Scheduled(cron = UPDATE_RESOURCE_DURATION)
    public void updateResource() {
        if (config.getStartInformer().equals(Const.OPTION_FALSE)) {
            return;
        }
        resourceInformerMap.values().forEach(K8sResourceInformer::replaceResource);
    }

    @Override
    public List<WorkLoadDTO> getWorkLoadByServiceInfo(String projectId, String namespace, String serviceName, String clusterId) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        List<T> serviceList = store.listByKindAndNamespace(Service.name(), namespace);
        List<WorkLoadDTO> result = new ArrayList<>();
        for (T service : serviceList) {
            io.fabric8.kubernetes.api.model.Service k8sService = (io.fabric8.kubernetes.api.model.Service) service;
            if (service.getMetadata().getLabels() == null || service.getMetadata().getLabels().isEmpty()
                    || k8sService.getSpec().getSelector() == null) {
                continue;
            }
            String appName = k8sService.getSpec().getSelector().get(meshConfig.getSelectorAppKey());
            if (projectId.equals(extractor.getResourceInfo(service, Const.RESOURCE_TARGET, projectId)) && serviceName.equals(appName)) {
                String key = appName
                        + Const.SEPARATOR_DOT
                        + k8sService.getMetadata().getNamespace();
                result.addAll(resourceCacheManager.getWorkloadListByServiceName(clusterId, key));
            }
        }
        result.forEach(workload->{
            workload.setInMesh(resourceCacheManager.isInjectedWorkload(clusterId,
                    workload.getKind(),
                    workload.getNamespace(),
                    workload.getName()));
        });
        return result;
    }

    @Override
    public List<PodDTO> getPodDtoByWorkLoadInfo(String clusterId, String kind, String namespace, String name) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        T obj = (T) store.get(kind, namespace, name);
        if (obj == null) {
            return new ArrayList<>();
        }
        return ((List<Pod>) store.listResourceByOwnerReference(Pod.name(), obj))
                .stream()
                .map(po -> createPodDto(po, clusterId))
                .collect(Collectors.toList());
    }

    private PodDTO createPodDto(Pod po, String clusterId) {
        PodDTO pod = new PodDTO(po, clusterId);
        Map<String, String> syncInfo = pilotHttpClient.getSidecarSyncStatus(po.getMetadata().getName(), po.getMetadata().getNamespace());
        pod.setSyncInfo(syncInfo);
        return pod;
    }

    private List<WorkLoadDTO> getWorkLoadFromServiceEntryEndpoint(List<Endpoint> endpointList){
        List<WorkLoadDTO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(endpointList)) {
            return result;
        }
        Map<String, List<Endpoint>> workloadMap = endpointList
                .stream()
                .collect(Collectors.groupingBy(ep -> ep.getHostname()));
        workloadMap.forEach((host, epList) -> {
            int nameIndex = host.indexOf(Const.SEPARATOR_DOT);
            String appName;
            String namespace;
            if (nameIndex < 0) {
                appName = host;
                namespace = null;
            } else {
                appName = host.substring(0, nameIndex);
                int namespaceIndex = host.indexOf(Const.SEPARATOR_DOT,nameIndex+1);
                if (namespaceIndex < 0){
                    namespace = host.substring(nameIndex+1);
                }else {
                    namespace = host.substring(nameIndex+1,namespaceIndex);
                }
            }
            String serviceName = appName + Const.SEPARATOR_DOT + namespace;

            WorkLoadDTO dto = new WorkLoadDTO();
            dto.setNamespace(namespace);
            dto.setName(appName);
            dto.setInMesh(true);
            dto.setKind("ServiceEntry");
            dto.setServiceName(serviceName);
            dto.setExternalService(true);
            dto.setServiceDomain(host);
            dto.setStatusInfo(epList.size()+"/"+epList.size());
            dto.setClusterId("virtualMachine");
            Map<String, String> labels = new HashMap<>();
            if (!CollectionUtils.isEmpty(epList)) {
                epList.forEach(ep -> {
                            if (ep.getLabels() != null) {
                                labels.putAll(ep.getLabels());
                            }
                        }
                );
            }
            dto.setLabels(labels);
            dto.setExternalServiceInstance(epList);
            result.add(dto);
        });
        return result;

    }

    @Override
    public List<WorkLoadDTO> getServiceEntryWorkLoad(String projectCode) {
        String projectKey = meshConfig.getProjectKey();
        Predicate<Endpoint> vmEndPointsForProject = ep-> ep.getLabels()!= null
                && projectCode.equals(ep.getLabels().get(projectKey));
        List<Endpoint> vmEndPoints = null;
        try {
            vmEndPoints = pilotHttpClient.getEndpointList(vmEndPointsForProject);
        } catch (Exception e) {
            log.warn("Get EndpointList from pilot error :{}",e.getMessage());
            return new ArrayList<>();
        }
        return getWorkLoadFromServiceEntryEndpoint(vmEndPoints);
    }

    @Override
    public List<WorkLoadDTO> getServiceEntryWorkloadByServiceInfo(String projectCode , String serviceName){
        String projectKey = meshConfig.getProjectKey();
        Predicate<Endpoint> vmEndPointsForProject = ep-> ep.getLabels()!= null
                && projectCode.equals(ep.getLabels().get(projectKey))
                && ep.getHostname().startsWith(serviceName);
        List<Endpoint> vmEndPoints = null;
        try {
            vmEndPoints = pilotHttpClient.getEndpointList(vmEndPointsForProject);
        } catch (Exception e) {
            log.warn("Get EndpointList from pilot error :{}",e.getMessage());
            return new ArrayList<>();
        }
        return getWorkLoadFromServiceEntryEndpoint(vmEndPoints);
    }

    @Override
    public List<T> getPodInfoByWorkLoadInfo(String clusterId, String kind, String namespace, String name) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        T obj = (T) store.get(kind, namespace, name);
        if (obj == null) {
            return new ArrayList<>();
        }
        return (List<T>) store.listResourceByOwnerReference(Pod.name(), obj);
    }

    @Override
    public List<WorkLoadDTO<T>> getAllWorkLoad(String projectId) {
        List<WorkLoadDTO<T>> workLoadList = new ArrayList<>();
        List<String> clusterIdList = ResourceStoreFactory.listClusterId();
        for (String clusterId : clusterIdList) {
            workLoadList.addAll(getAllWorkLoadByClusterId(clusterId, projectId));
        }
        return workLoadList;
    }

    @Override
    public List getAllWorkLoadByClusterId(String clusterId, String projectId) {
        List<WorkLoadDTO> workLoadList = new ArrayList<>();
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        List<T> serviceList = store.listByKind(Service.name());
        // 如果存在projectId 则过滤掉不含项目id的service
        if (!StringUtils.isEmpty(projectId)) {
            serviceList = serviceList.stream()
                    .filter(s ->StringUtils.isEmpty(projectId) || (s.getMetadata().getLabels() != null
                            && projectId.equals(extractor.getResourceInfo(s,Const.RESOURCE_TARGET,projectId))))
                    .collect(Collectors.toList());

        }
        for (T service : serviceList) {
            io.fabric8.kubernetes.api.model.Service k8sService = (io.fabric8.kubernetes.api.model.Service)service;
            if (k8sService.getSpec().getSelector() == null || k8sService.getSpec().getSelector().isEmpty()){
                continue;
            }
            String appName = k8sService.getSpec().getSelector().get(meshConfig.getSelectorAppKey());
            String key = appName
                    + Const.SEPARATOR_DOT
                    + k8sService.getMetadata().getNamespace();
            workLoadList.addAll(resourceCacheManager.getWorkloadListByServiceName(clusterId,key));
        }
        workLoadList.forEach(workload->{
            workload.setInMesh(resourceCacheManager.isInjectedWorkload(clusterId,
                    workload.getKind(),
                    workload.getNamespace(),
                    workload.getName()));
        });
        return workLoadList;
    }

    private List<T> getWorkLoadByServiceSelector(io.fabric8.kubernetes.api.model.Service service, String clusterId) {

        Map<String,String> selectorLabel = service.getSpec().getSelector();
        if (selectorLabel == null || selectorLabel.isEmpty()) {
            return new ArrayList<>();
        }

        return workLoadBySelectorCache.getUnchecked(new SelectorIndex(clusterId
                , service.getMetadata().getNamespace()
                , service.getMetadata().getName(), selectorLabel));
    }

    private String getProjectCodeFromService(T service) {
        if (service.getMetadata().getLabels() == null) {
            return null;
        }
        return extractor.getResourceInfo(service,Const.RESOURCE_TARGET);
    }

    private String getEnvNameFromService(T service) {
        if (service.getMetadata().getLabels() == null) {
            return null;
        }
        return service.getMetadata().getLabels().get(Const.LABEL_NSF_ENV);
    }

    @Override
    public List getWorkLoadByServiceInfoAllClusterId(String projectId, String namespace, String serviceName) {
        List<WorkLoadDTO> workLoadDtoList = new ArrayList<>();
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
    public List<PodDTO> getPodListWithSidecarVersion(List podDTOList, String expectedVersion) {
        if (CollectionUtils.isEmpty(podDTOList)) {
            return new ArrayList<>();
        }
        podDTOList.forEach(podDTO -> addSidecarVersionOnPod((PodDTO) podDTO, expectedVersion));
        return podDTOList;
    }


    @Override
    public List<T> getPodListByService(String clusterId, String namespace, String name) {

        List<T> result = new ArrayList<>();
        if (StringUtils.isEmpty(clusterId)) {
            List<String> clusterIdList = ResourceStoreFactory.listClusterId();
            if (!CollectionUtils.isEmpty(clusterIdList)) {
                for (String cluster : clusterIdList) {
                    result.addAll(getPodListByServiceAndClusterId(cluster, namespace, name));
                }
            }
        } else {
            result.addAll(getPodListByServiceAndClusterId(clusterId, namespace, name));
        }
        return result;
    }

    @Override
    public List<String> getMixerPathPatterns(String clusterId, String namespace, String app) {
        OwnerReferenceSupportStore<MixerUrlPattern> store = ResourceStoreFactory.getResourceStore(clusterId);
        MixerUrlPattern mup = store.get(MixerUrlPattern.name(), namespace, app);
        if (mup == null || mup.getSpec() == null || mup.getSpec().getPatterns() == null) {
            return Collections.emptyList();
        } else {
            return mup.getSpec().getPatterns();
        }
    }

    @Override
    public void deleteMixerPathPatterns(String clusterId, String namespace, String name) {
        OwnerReferenceSupportStore<MixerUrlPattern> store = ResourceStoreFactory.getResourceStore(clusterId);
        store.delete(MixerUrlPattern.name(), namespace, name);
    }

    @Override
    public void updateMixerPathPatterns(String clusterId, String namespace, String name, List<String> urlPatterns) {
        OwnerReferenceSupportStore<MixerUrlPattern> store = ResourceStoreFactory.getResourceStore(clusterId);
        MixerUrlPattern mup = new MixerUrlPatternBuilder()
            .withNewMetadata()
            .withName(name)
            .withNamespace(namespace)
            .and()
            .withNewSpec()
            .withPatterns(urlPatterns)
            .and()
            .build();
        multiClusterK8sClient.k8sClient(clusterId).createOrUpdate(mup, ResourceType.OBJECT);
    }

    @Override
    public String getAppNameByPod(String clusterId, String namespace, String name) {
        return getPodLabel(clusterId, namespace, name, meshConfig.getSelectorAppKey());
    }

    @Override
    public String getPodLabel(String clusterId, String namespace, String name, String labelName) {
        OwnerReferenceSupportStore<Pod> store = ResourceStoreFactory.getResourceStore(clusterId);
        Pod pod = store.get(Pod.name(), namespace, name);
        if (pod == null || pod.getMetadata() == null || pod.getMetadata().getLabels() == null) {
            return "";
        } else {
            return Strings.nullToEmpty(pod.getMetadata().getLabels().get(labelName));
        }
    }

    private List<T> getPodListByServiceAndClusterId(String clusterId, String namespace, String name) {

        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        Endpoints endpointsByService = (Endpoints) store.get(Endpoints.name(), namespace, name);
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

        if (CollectionUtils.isEmpty(podReferences)) {
            return new ArrayList<>();
        }
        return (List<T>) podReferences.stream()
                .map(ref -> store.get(ref.getKind(), ref.getNamespace(), ref.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Endpoints> getEndPointByService(String clusterId, String namespace, String name) {
        List<Endpoints> result = new ArrayList<>();
        if (StringUtils.isEmpty(clusterId)) {
            List<String> clusterIdList = ResourceStoreFactory.listClusterId();
            if (!CollectionUtils.isEmpty(clusterIdList)) {
                for (String cluster : clusterIdList) {
                    result.add(getEndPointByServiceAndClusterId(cluster, namespace, name));
                }
            }
        } else {
            result.add(getEndPointByServiceAndClusterId(clusterId, namespace, name));
        }
        return result;
    }

    @Override
    public List<ServiceDto> getServiceByProjectCode(String projectCode, String clusterId) {
        List<ServiceDto> serviceDtoList = new ArrayList<>();
        if (StringUtils.isEmpty(clusterId)){
            for (String cluster : ResourceStoreFactory.listClusterId()) {
                serviceDtoList.addAll(getServiceByProjectCodeAndClusterId(projectCode,cluster));
            }
        }else {
            serviceDtoList = getServiceByProjectCodeAndClusterId(projectCode, clusterId);
        }
        return serviceDtoList;
    }

    @Override
    public List<WorkLoadDTO> getWorkLoadByApp(String namespace, String appName, String clusterId) {
        String serviceName = appName + "." + namespace;
        return getWorkLoadByIndex(clusterId, namespace, appName)
                .stream().map(obj -> new WorkLoadDTO<>(obj, serviceName, clusterId))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkLoadDTO> getWorkLoadByAppAllClusterId(String namespace, String appName) {
        List<WorkLoadDTO> workLoadDTOList = new ArrayList<>();

        for (String cluster : ResourceStoreFactory.listClusterId()) {
            workLoadDTOList.addAll(getWorkLoadByApp(namespace, appName, cluster));
        }

        return workLoadDTOList;
    }

    @Override
    public List<WorkLoadDTO> getWorkLoadByLabelsInAnyClusterId(List<String> labelsList, String namespace){
        List<String> clusterIdList = ResourceStoreFactory.listClusterId();
        List<WorkLoadDTO> workLoadDTOSInAnyCluster = new ArrayList<>();
        if (CollectionUtils.isEmpty(clusterIdList)){
            return workLoadDTOSInAnyCluster;
        }
        for (String clusterId : clusterIdList) {
            workLoadDTOSInAnyCluster.addAll(getWorkLoadByLabels(clusterId,labelsList,namespace));
        }
        return workLoadDTOSInAnyCluster;
    }

    @Override
    public List<WorkLoadDTO> getWorkLoadByLabels(String clusterId, List<String> labelsList, String namespace) {
        Map<String,String> labels = new HashMap<>();
        if (!CollectionUtils.isEmpty(labelsList)){
            for (String label : labelsList) {
                int index = label.indexOf("=");
                if (index < 0){
                    continue;
                }
                String key = label.substring(0,index);
                String value = label.substring(index+1);
                labels.put(key,value);
            }
        }
        KubernetesClient kubernetesClient = multiClusterK8sClient.k8sClient(clusterId);
        List<T> workloadList = new ArrayList<>();
        workloadList.addAll(kubernetesClient.getObjectList(Deployment.name(),namespace,labels));
        workloadList.addAll(kubernetesClient.getObjectList(StatefulSet.name(),namespace,labels));
        if (CollectionUtils.isEmpty(workloadList)){
            return new ArrayList<>();
        }
        Set<WorkLoadDTO> result = new HashSet();
        for (T workload : workloadList) {
            String serviceNameByWorkload = resourceCacheManager.getServiceNameByWorkload(workload);
            if (!StringUtils.isEmpty(serviceNameByWorkload)){
                result.addAll(resourceCacheManager.getWorkloadListByServiceName(clusterId,serviceNameByWorkload));
            }
        }
        result.forEach(workload->{
            workload.setInMesh(resourceCacheManager.isInjectedWorkload(clusterId,
                    workload.getKind(),
                    workload.getNamespace(),
                    workload.getName()));
        });
        return new ArrayList<>(result);

    }

    @Override
    public List<String> getSidecarVersionOnWorkLoad(String clusterId, String namespace, String kind, String name) {
        List<PodDTO> podByWorkLoadInfo = getPodDtoByWorkLoadInfo(clusterId, kind, namespace, name);
        PodVersion queryVersion = new PodVersion();
        queryVersion.setClusterId(clusterId);
        queryVersion.setNamespace(namespace);
        queryVersion.setPodNames(podByWorkLoadInfo
                .stream()
                .map(pod -> pod.getName())
                .collect(Collectors.toList()));
        List<PodStatus> podStatuses = versionManagerService.queryByPodNameList(queryVersion);
        if (CollectionUtils.isEmpty(podStatuses)) {
            return new ArrayList<>();
        }
        Set<String> versionSet = podStatuses.stream().
                map(podStatus -> podStatus.getCurrentVersion())
                .collect(Collectors.toSet());
        return new ArrayList<>(versionSet);
    }

    private List<ServiceDto> getServiceByProjectCodeAndClusterId(String projectCode, String clusterId) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        return  ((List<T>)store.listByKind(Service.name()))
                .stream()
                .filter(s->s.getMetadata().getLabels()!=null&&!s.getMetadata().getLabels().isEmpty()
                        &&extractor.getResourceInfo(s,Const.RESOURCE_TARGET,projectCode)!=null
                        &&extractor.getResourceInfo(s,Const.RESOURCE_TARGET,projectCode).equals(projectCode))
                .map(s->{
                    ServiceDto<T> tServiceDto = new ServiceDto<>(s, clusterId);
                    if (tServiceDto.getSelectLabels() != null){
                        tServiceDto.setAppName(tServiceDto.getSelectLabels().get(meshConfig.getSelectorAppKey()));

                    }
                    return tServiceDto;
                })
                .collect(Collectors.toList());
    }

    private Endpoints getEndPointByServiceAndClusterId(String clusterId, String namespace, String name) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        return (Endpoints) store.get(Endpoints.name(), namespace, name);
    }


    private PodDTO addSidecarVersionOnPod(PodDTO podDTO, String expectedVersion) {
        PodVersion queryVersion = new PodVersion();
        queryVersion.setClusterId(podDTO.getClusterId());
        queryVersion.setNamespace(podDTO.getNamespace());
        queryVersion.setPodNames(Arrays.asList(podDTO.getName()));
        List<PodStatus> podStatuses = versionManagerService.queryByPodNameList(queryVersion);
        if (CollectionUtils.isEmpty(podStatuses)) {
            podDTO.setSidecarContainerStatus(Const.SIDECAR_CONTAINER_ERROR);
            if (podDTO.isInjected()){
                podDTO.setVersionManagerCrdStatus(Const.VERSION_MANAGER_CRD_MISSING);
                log.info("no sidecar status for pod[{}] and pod is injected",podDTO.getName());
            }else {
                podDTO.setVersionManagerCrdStatus(Const.VERSION_MANAGER_CRD_DEFAULT);
                log.info("no sidecar status for pod[{}] and pod is not injected",podDTO.getName());
            }
            return podDTO;
        }
        podDTO.setVersionManagerCrdStatus(Const.VERSION_MANAGER_CRD_EXIST);
        PodStatus status = podStatuses.get(0);
        if (StringUtils.isEmpty(status.getExpectedVersion())
                ||StringUtils.isEmpty(status.getCurrentVersion())
                ||!status.getCurrentVersion().equals(status.getExpectedVersion())
                ||StringUtils.isEmpty(expectedVersion)
                ||!expectedVersion.equals(status.getExpectedVersion()))
        {
            podDTO.setSidecarContainerStatus(Const.SIDECAR_CONTAINER_ERROR);
        }else {
            podDTO.setSidecarContainerStatus(Const.SIDECAR_CONTAINER_SUCCESS);
        }
        if (status.getCurrentVersion().equals(Const.DEFAULT_SIDECAR)){
            podDTO.setSidecarStatus(getSidecarVersionFromImage(podDTO.getSidecarImage()));
        }else {
            podDTO.setSidecarStatus(status.getCurrentVersion());
        }
        return podDTO;
    }

    private String getSidecarVersionFromImage(String sidecarImage){
        Pattern pattern = Pattern.compile(Const.SIDECAR_VERSION_PATTERN);
        Matcher matcher = pattern.matcher(sidecarImage);
        if (matcher.find()){
            return matcher.group(0);
        }else {
            return Const.DEFAULT_SIDECAR;
        }
    }


    private WorkLoadDTO<T> addSidecarVersionOnWorkLoad(WorkLoadDTO<T> workLoadDTO) {
        List<PodDTO> podByWorkLoadInfo = getPodDtoByWorkLoadInfo(workLoadDTO.getClusterId(), workLoadDTO.getKind(), workLoadDTO.getNamespace(),
                workLoadDTO.getName());
        PodVersion queryVersion = new PodVersion();
        queryVersion.setClusterId(workLoadDTO.getClusterId());
        queryVersion.setNamespace(workLoadDTO.getNamespace());
        queryVersion.setPodNames(podByWorkLoadInfo
                .stream()
                .map(pod -> pod.getName())
                .collect(Collectors.toList()));
        List<PodStatus> podStatuses = versionManagerService.queryByPodNameList(queryVersion);
        if (CollectionUtils.isEmpty(podStatuses)) {
            return workLoadDTO;
        }
        Set<String> versionSet = podStatuses.stream().
                map(podStatus -> podStatus.getCurrentVersion())
                .collect(Collectors.toSet());
        workLoadDTO.setSidecarVersion(new ArrayList<>(versionSet));
        if (StringUtils.isEmpty(getLabelValueFromWorkLoad(workLoadDTO,meshConfig.getAppKey()))
            ||StringUtils.isEmpty(getLabelValueFromWorkLoad(workLoadDTO,meshConfig.getVersionKey()))
            ||CollectionUtils.isEmpty(workLoadDTO.getSidecarVersion())){
            workLoadDTO.setInMesh(false);
        }else {
            workLoadDTO.setInMesh(true);
        }

        return workLoadDTO;
    }

    @Override
    public List<T> getServiceByClusterAndNamespace(String clusterId,String namespace){
        List<T> serviceList = new ArrayList<>();
        if (StringUtils.isEmpty(clusterId)){
            for (String c : ResourceStoreFactory.listClusterId()) {
                OwnerReferenceSupportStore resourceStore = ResourceStoreFactory.getResourceStore(c);
                serviceList.addAll(resourceStore.listByKindAndNamespace(Service.name(),namespace));
            }
        }else {
            serviceList = ResourceStoreFactory.getResourceStore(clusterId).listByKindAndNamespace(Service.name(),namespace);
        }
        return serviceList;
    }

    private String getLabelValueFromWorkLoad(WorkLoadDTO obj,String key){
        if (obj.getLabels() == null){
            return null;
        }
        return  (obj.getLabels().get(key) == null)?null:String.valueOf(obj.getLabels().get(key));
    }

    private List<T> getWorkLoadByIndex(String clusterId, String namespace, String name) {
        return workLoadByServiceCache.getUnchecked(new WorkLoadIndex(clusterId, namespace, name));
    }

    private List<T> doGetWorkLoadListByApp(String clusterId, String namespace, String appName) {
        OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore(clusterId);
        List<T> workLoadList = new ArrayList<>();
        workLoadList.addAll(store.listByKindAndNamespace(Deployment.name(), namespace));
        workLoadList.addAll(store.listByKindAndNamespace(StatefulSet.name(), namespace));
        if (CollectionUtils.isEmpty(workLoadList) || StringUtils.isEmpty(appName)) {
            return workLoadList;
        }
        workLoadList = workLoadList.stream()
                .filter(w -> w.getMetadata().getLabels() != null
                        && appName.equals(w.getMetadata().getLabels().get(meshConfig.getAppKey())))
                .collect(Collectors.toList());

//        Endpoints endpointsByService = (Endpoints) store.get(Endpoints.name(), namespace, serviceName);
//        if (endpointsByService == null) {
//            return new ArrayList<>();
//        }
//        //从endpoints信息中解析出关联的pod列表
//        List<ObjectReference> podReferences = endpointsByService.getSubsets()
//                .stream()
//                .flatMap(sub -> sub.getAddresses().stream())
//                .map(EndpointAddress::getTargetRef)
//                .filter(Objects::nonNull)
//                .filter(ref -> Pod.name().equals(ref.getKind()))
//                .collect(Collectors.toList());
//
//        Set<T> workLoadList = new HashSet<>();
//        // 通过pod信息查询全部的负载资源
//        if (!CollectionUtils.isEmpty(podReferences)) {
//            podReferences.forEach(podRef -> {
//                T pod = (T) store.get(Pod.name(), podRef.getNamespace(), podRef.getName());
//                if (pod != null) {
//                    workLoadList.addAll(store.listLoadByPod(pod));
//                }
//            });
//        }
        return workLoadList;
    }

    private List<T> doGetWorkLoadListBySelector(String clusterId, String namespace,Map<String,String> labels) {
        KubernetesClient kubernetesClient = multiClusterK8sClient.k8sClient(clusterId);
        List<T> workLoadList = new ArrayList<>();
        if (labels == null || labels.isEmpty()){
            return workLoadList;
        }
        workLoadList.addAll(kubernetesClient.getObjectList(Deployment.name(),namespace,labels));
        workLoadList.addAll(kubernetesClient.getObjectList(StatefulSet.name(),namespace,labels));

        return workLoadList;
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
                        .inAnyNamespace(),client.watchResource));
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
                        .inAnyNamespace(),client.watchResource));
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
                        .inAnyNamespace(),client.watchResource));
            }
        });
        return result;
    }

    private List<MixedOperation> getMixerUrlPatternMixedOperationList(Map<String, MultiClusterK8sClient.ClientSet> cm, CustomResourceDefinition mupCrd) {
        List<MixedOperation> result = new ArrayList<>();
        cm.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
            	result.add(new ClusterMixedOperation(name, (MixedOperation)client.originalK8sClient
                    .customResources(mupCrd, MixerUrlPattern.class, MixerUrlPatternList.class, DoneableMixerUrlPattern.class)
                    .inAnyNamespace(),client.watchResource));
            }
        });
        return result;
    }

    private List<MixedOperation> getMixerVersionManagerOperationList(Map<String, MultiClusterK8sClient.ClientSet> cm, CustomResourceDefinition vmcrd) {
        List<MixedOperation> result = new ArrayList<>();
        cm.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterMixedOperation(name, (MixedOperation)client.originalK8sClient
                        .customResources(vmcrd, VersionManager.class, VersionManagerList.class, DoneableVersionManager.class)
                        .inAnyNamespace(),client.watchResource));
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
                        .inAnyNamespace(),client.watchResource));
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
                        .inAnyNamespace(),client.watchResource));
            }
        });
        return result;
    }

    private List<MixedOperation> getDaemonSetMixedOperationList(Map<String, MultiClusterK8sClient.ClientSet> cm) {
        List<MixedOperation> result = new ArrayList<>();
        cm.forEach((name, client) -> {
            if (!StringUtils.isEmpty(name)) {
                result.add(new ClusterMixedOperation(name, (MixedOperation) client.originalK8sClient
                        .apps()
                        .daemonSets()
                        .inAnyNamespace(),client.watchResource));
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
                        .inAnyNamespace(),client.watchResource));
            }
        });
        return result;
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

    class SelectorIndex {

        private String clusterId;
        private String namespace;
        private String serviceName;
        private Map<String,String> selectorLabels;

        public SelectorIndex(String clusterId, String namespace, String serviceName, Map<String, String> selectorLabels) {
            this.clusterId = clusterId;
            this.namespace = namespace;
            this.serviceName = serviceName;
            this.selectorLabels = selectorLabels;
        }

        public String getClusterId() {
            return clusterId;
        }

        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public Map<String, String> getSelectorLabels() {
            return selectorLabels;
        }

        public void setSelectorLabels(Map<String, String> selectorLabels) {
            this.selectorLabels = selectorLabels;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SelectorIndex that = (SelectorIndex) o;
            return Objects.equals(clusterId, that.clusterId) &&
                    Objects.equals(namespace, that.namespace) &&
                    Objects.equals(serviceName, that.serviceName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clusterId, namespace, serviceName);
        }
    }


}
