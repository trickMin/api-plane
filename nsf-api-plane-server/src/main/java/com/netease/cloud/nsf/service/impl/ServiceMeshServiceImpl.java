package com.netease.cloud.nsf.service.impl;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.cache.OwnerReferenceSupportStore;
import com.netease.cloud.nsf.cache.ResourceCache;
import com.netease.cloud.nsf.cache.ResourceStoreFactory;
import com.netease.cloud.nsf.cache.extractor.ResourceExtractorManager;
import com.netease.cloud.nsf.cache.meta.PodDTO;
import com.netease.cloud.nsf.configuration.ext.ApiPlaneConfig;
import com.netease.cloud.nsf.configuration.ext.MeshConfig;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.istio.PilotHttpClient;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import com.netease.cloud.nsf.core.servicemesh.MultiK8sConfigStore;
import com.netease.cloud.nsf.core.servicemesh.ServiceMeshConfigManager;
import com.netease.cloud.nsf.meta.SVMSpec;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import com.netease.cloud.nsf.meta.TrafficMarkConfigDto;
import com.netease.cloud.nsf.meta.dto.ResourceWrapperDTO;
import com.netease.cloud.nsf.service.ServiceMeshService;
import com.netease.cloud.nsf.service.VersionManagerService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.RestTemplateClient;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.GlobalConfig;
import me.snowdrop.istio.api.networking.v1alpha3.GlobalConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.netease.cloud.nsf.core.editor.ResourceType.OBJECT;
import static com.netease.cloud.nsf.core.k8s.K8sResourceEnum.DaemonSet;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/7
 **/
@Service
public class ServiceMeshServiceImpl<T extends HasMetadata> implements ServiceMeshService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceMeshServiceImpl.class);
    private static final String DEFAULT_SIDECAR_VERSION = "envoy";
    private static final String DEFAULT_SERVICE_SELECTOR_KEY = "app";
    private static final long PROJECT_CACHE_MAX_SIZE = 200;
    private static final long PROJECT_CACHE_REFRESH_DURATION = 3;
    public static final String ISTIO_ENV = "istio-env";
    public static final String ISTIO_REV = "istio.io/rev";
    private ExecutorService taskPool = Executors.newCachedThreadPool();
    private LoadingCache<App2ProjectIndex, String> app2ProjectCache = CacheBuilder.newBuilder()
            .maximumSize(PROJECT_CACHE_MAX_SIZE)
            .refreshAfterWrite(PROJECT_CACHE_REFRESH_DURATION, TimeUnit.SECONDS)
            .build(new CacheLoader<App2ProjectIndex, String>() {
                @Override
                public String load(App2ProjectIndex index) {
                    return doGetProjectCodeByApp(index.getNamespace(), index.getAppName(), index.getClusterId());
                }

            });

    @Autowired
    MultiK8sConfigStore configStore;

    @Autowired
    ResourceCache k8sResource;


    @Autowired
    VersionManagerService versionManagerService;

    @Autowired
    RestTemplateClient restTemplateClient;

    @Autowired
    ApiPlaneConfig apiPlaneConfig;

    @Autowired
    PilotHttpClient pilotHttpClient;

    @Autowired
    MultiClusterK8sClient multiClusterK8sClient;

    @Autowired
    MeshConfig meshConfig;

    @Autowired
    ResourceExtractorManager extractor;

    @Autowired
    ServiceMeshConfigManager configManager;

    @Override
    public void updateIstioResource(String json, String clusterId) {
        json = optimize(json);
        clusterId = StringUtils.isEmpty(clusterId) ? getDefaultClusterId() : clusterId;
        configStore.update(json2Resource(json), clusterId);
    }

    @Override
    public void deleteIstioResource(String json, String clusterId) {
        json = optimize(json);
        clusterId = StringUtils.isEmpty(clusterId) ? getDefaultClusterId() : clusterId;
        configStore.delete(json2Resource(json), clusterId);
    }

    @Override
    public List<ResourceWrapperDTO> getIstioResourceList(String clusterId, String namespaces, String kind) {

        if (StringUtils.isEmpty(namespaces)) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST, 404);
        List<ResourceWrapperDTO> wrapperDTOS = new ArrayList<>();
        clusterId = StringUtils.isEmpty(clusterId) ? getDefaultClusterId() : clusterId;

        for (String ns : namespaces.split(",")) {
            if (StringUtils.isEmpty(ns)) continue;
            List<HasMetadata> resources;
            try {
                resources = configStore.getList(kind, ns, clusterId);
            } catch (Exception e) {
                logger.warn("find resources failed", e);
                continue;
            }
            for (HasMetadata r : resources) {
                wrapperDTOS.add(new ResourceWrapperDTO(r, clusterId));
            }
        }
        return wrapperDTOS;
    }

    @Override
    public HasMetadata getIstioResource(String clusterId, String name, String namespace, String kind) {
        clusterId = StringUtils.isEmpty(clusterId) ? getDefaultClusterId() : clusterId;
        HasMetadata resource = configStore.get(kind, namespace, name, clusterId);
        if (resource == null) {
            throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST, 404);
        }
        return resource;
    }

    @Override
    public String invokeIstiodApi(String clusterId, String podName, String namespace, String path) {
        clusterId = StringUtils.isEmpty(clusterId) ? getDefaultClusterId() : clusterId;
        return pilotHttpClient.invokeIstiod(podName, namespace, path, null);
    }

    @Override
    public List<String> getIstiodInstances(String type, String version) {
        if (type.equals("istio-env")) {
            String namespace = version;
            String deployName = "istio-pilot";
            OwnerReferenceSupportStore store = ResourceStoreFactory.getResourceStore("default");
            T obj = (T) store.get("Deployment", namespace, deployName);
            if (obj == null) {
                return new ArrayList<>();
            }
            return ((List<Pod>) store.listResourceByOwnerReference(K8sResourceEnum.Pod.name(), obj))
                .stream()
                .map(po -> po.getMetadata().getName() + "." + po.getMetadata().getNamespace())
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public ErrorCode sidecarInject(String clusterId, String kind, String namespace, String name, String version, String expectedVersion, String appName) {
        if (!K8sResourceEnum.StatefulSet.name().equals(kind) && !K8sResourceEnum.Deployment.name().equals(kind)) {
            return ApiPlaneErrorCode.MissingParamsError("resource kind");
        }
        T resourceToInject = (T) k8sResource.getResource(clusterId, kind, namespace, name);
        if (resourceToInject == null) {
            return ApiPlaneErrorCode.workLoadNotFound;
        }
        if (!checkEnable(namespace, clusterId)) {
            return ApiPlaneErrorCode.sidecarInjectPolicyError;
        }
        Map<String, String> labels = new HashMap<>(1);
        Map<String, String> injectAnnotation = new HashMap<>(1);
        labels.put(meshConfig.getVersionKey(), version);
        labels.put(meshConfig.getAppKey(), appName);
        injectAnnotation.put(Const.ISTIO_INJECT_ANNOTATION, "true");
        Map<String, String> workloadAnnotation = new HashMap<>(2);
        workloadAnnotation.put(Const.WORKLOAD_OPERATION_TYPE_ANNOTATION, Const.WORKLOAD_OPERATION_TYPE_ANNOTATION_INJECT);
        workloadAnnotation.put(Const.WORKLOAD_UPDATE_TIME_ANNOTATION, String.valueOf(System.currentTimeMillis()));
        T injectedWorkLoad = appendLabel(appendAnnotationToPod(resourceToInject, injectAnnotation), labels);
        injectedWorkLoad = appendAnnotationOnWorkload(injectedWorkLoad, workloadAnnotation);
        updateResource(injectedWorkLoad, clusterId);
        createSidecarVersionCRD(clusterId, namespace, kind, name, expectedVersion);
        return ApiPlaneErrorCode.Success;
    }

    @Override
    public void notifySidecarFileEvent(String sidecarVersion, String type) {
        List<String> clusterIds = ResourceStoreFactory.listClusterId();
        for (String clusterId : clusterIds) {
            List<T> podByWorkLoadInfo = k8sResource.getPodInfoByWorkLoadInfo(clusterId, DaemonSet.name(),
                    apiPlaneConfig.getDaemonSetNamespace(), apiPlaneConfig.getDaemonSetName());
            logger.info("get {} daemonSet with namespace [{}] and name [{}]", podByWorkLoadInfo.size(),
                    apiPlaneConfig.getDaemonSetNamespace(), apiPlaneConfig.getDaemonSetName());
            if (!CollectionUtils.isEmpty(podByWorkLoadInfo)) {
                Set<String> notified = new HashSet<>();
                for (T pod : podByWorkLoadInfo) {
                    Pod p = (Pod) pod;
                    String hostAddress = p.getStatus().getPodIP();
                    if (!notified.contains(hostAddress)) {
                        notified.add(hostAddress);
                        taskPool.execute(() -> {
                            try {
                                doNotify(hostAddress, sidecarVersion, type);
                            } catch (Exception e) {
                                logger.error("notify sidecar event to Pod[{}] error", pod.getMetadata().getName());
                            }
                        });
                    }
                }
            }
        }
    }

    private void doNotify(String host, String sidecarVersion, String type) {
        Map<String, Object> param = new HashMap<>();
        param.put("sidecarVersion", sidecarVersion);
        param.put("type", type);
        String url = host +
                ":" +
                apiPlaneConfig.getDaemonSetPort() +
                "/api/servicemesh?Action=EnvoyEvent&Version=2019-01-02&"
                + "SidecarVersion={sidecarVersion}"
                + "&Type={type}";
        restTemplateClient.getForValue(url, param, Const.GET_METHOD, String.class);
    }


    private T appendLabel(T obj, Map<String, String> label) {
        Map<String, String> currentLabel = obj.getMetadata().getLabels();
        obj.getMetadata()
                .setLabels(appendKeyValue(currentLabel, label));
        return obj;
    }

    private T appendAnnotationToPod(T obj, Map<String, String> annotations) {
        Map<String, String> currentAnnotations = null;
        if (obj instanceof Deployment) {
            Deployment deployment = (Deployment) obj;
            currentAnnotations = deployment.getSpec()
                    .getTemplate()
                    .getMetadata()
                    .getAnnotations();

            deployment.getSpec()
                    .getTemplate()
                    .getMetadata()
                    .setAnnotations(appendKeyValue(currentAnnotations, annotations));
        } else if (obj instanceof StatefulSet) {
            StatefulSet statefulSet = (StatefulSet) obj;
            currentAnnotations = statefulSet.getSpec()
                    .getTemplate()
                    .getMetadata()
                    .getAnnotations();

            statefulSet.getSpec()
                    .getTemplate()
                    .getMetadata()
                    .setAnnotations(appendKeyValue(currentAnnotations, annotations));
        }
        return obj;
    }

    private T appendAnnotationOnWorkload(T obj, Map<String, String> annotations) {

        Map<String, String> currentAnnotations = obj.getMetadata().getAnnotations();
        if (currentAnnotations == null) {
            currentAnnotations = new HashMap<>();
        }
        obj.getMetadata().setAnnotations(appendKeyValue(currentAnnotations, annotations));
        return obj;
    }

    private Map<String, String> appendKeyValue(Map<String, String> current, Map<String, String> update) {
        if (update == null || update.isEmpty()) {
            return current;
        }
        if (current == null || current.isEmpty()) {
            current = update;
        } else {
            for (Map.Entry<String, String> kv : update.entrySet()) {
                current.put(kv.getKey(), kv.getValue());
            }
        }
        return current;
    }


    private String optimize(String json) {
        if (json.startsWith("\"") && json.startsWith("\"")) {
            json = json.substring(1, json.length() - 1);
        }
        return json;
    }

    private IstioResource json2Resource(String json) {
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(json, ResourceType.JSON);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        return (IstioResource) gen.object(resourceEnum.mappingType());
    }

    private boolean isInvalidApiVersion(String errorMsg) {
        return errorMsg.contains("does not match the expected API");
    }

    private boolean checkEnable(String namespace, String clusterId) {
        KubernetesClient kubernetesClient = multiClusterK8sClient.k8sClient(clusterId);
        String url = kubernetesClient.getUrl("Namespace", namespace);
        Namespace ns = kubernetesClient.getObject(url);
        Map<String, String> labels = ns.getMetadata().getLabels();
        if (labels == null || labels.isEmpty()) {
            return true;
        }
        String namespaceInjection = labels.get(Const.LABEL_NAMESPACE_INJECTION);
        if (!StringUtils.isEmpty(namespaceInjection) && Const.OPTION_DISABLED.equals(namespaceInjection)) {
            return false;
        }
        return true;
    }

    @Override
    public void createSidecarVersionCRD(String clusterId, String namespace, String kind, String name, String expectedVersion) {
        SidecarVersionManagement versionManagement = new SidecarVersionManagement();
        versionManagement.setClusterId(clusterId);
        versionManagement.setNamespace(namespace);
        SVMSpec svmSpec = new SVMSpec();
        svmSpec.setWorkLoadType(kind);
        svmSpec.setWorkLoadName(name);
        svmSpec.setExpectedVersion(expectedVersion);
        if (StringUtils.isEmpty(expectedVersion)) {
            svmSpec.setExpectedVersion(DEFAULT_SIDECAR_VERSION);
        }
        versionManagement.setWorkLoads(Arrays.asList(svmSpec));
        versionManagerService.updateSVM(versionManagement);
    }

    @Override
    public void createMissingCrd(List podList, String workLoadType, String workLoadName, String clusterId, String namespace) {

        boolean createCrd = false;
        for (Object o : podList) {
            PodDTO dto = (PodDTO) o;
            if (dto.getVersionManagerCrdStatus() == Const.VERSION_MANAGER_CRD_MISSING) {
                createCrd = true;
                break;
            }
        }
        if (createCrd) {
            taskPool.execute(() -> {
                logger.info("auto create VersionManager for workload[{}]", workLoadName);
                createSidecarVersionCRD(clusterId, namespace, workLoadType, workLoadName, null);
            });
        }
    }

    @Override
    public boolean checkPilotHealth() {
        return pilotHttpClient.isReady();
    }

    @Override
    public ErrorCode removeInject(String clusterId, String kind, String namespace, String name) {
        if (!K8sResourceEnum.StatefulSet.name().equals(kind) && !K8sResourceEnum.Deployment.name().equals(kind)) {
            return ApiPlaneErrorCode.MissingParamsError("resource kind");
        }
        T resourceToInject = (T) k8sResource.getResource(clusterId, kind, namespace, name);
        if (resourceToInject == null) {
            return ApiPlaneErrorCode.workLoadNotFound;
        }
        Map<String, String> injectAnnotation = new HashMap<>(3);
        injectAnnotation.put(Const.ISTIO_INJECT_ANNOTATION, "false");
        Map<String, String> workloadAnnotation = new HashMap<>(2);
        workloadAnnotation.put(Const.WORKLOAD_OPERATION_TYPE_ANNOTATION, Const.WORKLOAD_OPERATION_TYPE_ANNOTATION_EXIT);
        workloadAnnotation.put(Const.WORKLOAD_UPDATE_TIME_ANNOTATION, String.valueOf(System.currentTimeMillis()));
        T injectedWorkLoad = appendAnnotationToPod(resourceToInject, injectAnnotation);
        injectedWorkLoad = appendAnnotationOnWorkload(injectedWorkLoad, workloadAnnotation);
        updateResource(injectedWorkLoad, clusterId);
        return ApiPlaneErrorCode.Success;
    }

    @Override
    public Map<String, List<String>> getServiceMeshTrafficMarks(List<String> services) {
        return services.stream()
            .filter(n -> n != null && n.contains("."))
            .distinct()
            .flatMap(n -> {
                int dotInd = n.lastIndexOf('.');
                HasMetadata svc = k8sResource.getResource("default", K8sResourceEnum.Service.name(), n.substring(dotInd + 1), n.substring(0, dotInd));
                return Stream.of(svc)
                    .filter(Objects::nonNull)
                    .map(s -> s.getMetadata().getAnnotations())
                    .filter(Objects::nonNull)
                    .map(a -> a.get(Const.MARK_SM_ANNOTATION))
                    .filter(Objects::nonNull)
                    .map(mark -> Pair.of(n, mark.isEmpty() ? Collections.<String>emptyList() : Arrays.asList(mark.split(","))));
            })
			.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    @Override
    public void updateNsfTrafficMarkAnnotations(TrafficMarkConfigDto config) {
        String clusterId = MultiClusterK8sClient.DEFAULT_CLUSTER_NAME;
        OwnerReferenceSupportStore<HasMetadata> store = ResourceStoreFactory.getResourceStore(clusterId);
        io.fabric8.kubernetes.client.KubernetesClient client = multiClusterK8sClient.originalK8sClient(clusterId);

        Map<String, HasMetadata> services = store.listByKind(K8sResourceEnum.Service.name())
            .stream()
            .collect(Collectors.toMap(s -> s.getMetadata().getName() + "." + s.getMetadata().getNamespace(), s -> s));
        String switchMarkString = config.getEnabledMarks().stream().sorted().collect(Collectors.joining(","));
        config.getServiceNames().forEach(svcName -> {
            HasMetadata svc = services.get(svcName);
            if (svc == null) {
                return;
            }
            String nsfMarkString = config.getMappings().getOrDefault(svcName, Collections.emptySet())
                .stream().sorted().collect(Collectors.joining(","));
            if (svc.getMetadata().getAnnotations() == null ||
				!switchMarkString.equals(svc.getMetadata().getAnnotations().get(Const.MARK_SWITCH_ANNOTATION)) ||
                !nsfMarkString.equals(svc.getMetadata().getAnnotations().get(Const.MARK_NSF_ANNOTATION))) {
                client
                    .services()
                    .inNamespace(svc.getMetadata().getNamespace())
                    .withName(svc.getMetadata().getName())
                    .edit()
                    .editMetadata()
                    .addToAnnotations(Const.MARK_SWITCH_ANNOTATION, switchMarkString)
                    .addToAnnotations(Const.MARK_NSF_ANNOTATION, nsfMarkString)
                    .endMetadata()
                    .done();
            }
        });
    }

    @Override
    public ErrorCode createAppOnService(String clusterId, String namespace, String name, String appName) {
        if (StringUtils.isEmpty(clusterId)) {
            for (String cluster : ResourceStoreFactory.listClusterId()) {
                createAppOnServiceByClusterId(cluster, namespace, name, appName);
            }
        } else {
            createAppOnServiceByClusterId(clusterId, namespace, name, appName);
        }
        return ApiPlaneErrorCode.Success;
    }

    private long lastLogTime = System.currentTimeMillis();

    @Override
    public String getProjectCodeByApp(String namespace, String appName, String clusterId) {

        try {
            return app2ProjectCache.getUnchecked(new App2ProjectIndex(appName, namespace, clusterId));
        } catch (CacheLoader.InvalidCacheLoadException e) {
            if (System.currentTimeMillis() - lastLogTime > 5000) {
                logger.warn("can`t find projectCode by app[{}] and namespace[{}]", appName, namespace);
                lastLogTime = System.currentTimeMillis();
            }
            return null;
        }

    }

    @Override
    public void updateDefaultSidecarVersion(String defaultSidecarVersion) {
        Set<String> clusterNames = multiClusterK8sClient.getAllClients().keySet();
        if (!CollectionUtils.isEmpty(clusterNames)) {
            for (String clusterId : clusterNames) {
                try {
                    updateVersionManagerDefaultVersion(defaultSidecarVersion, clusterId);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn("update cluster {} default sidecar version failure.", clusterId, e);
                }
            }
        }
    }

    private void updateVersionManagerDefaultVersion(String defaultSidecarVersion, String clusterId) {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetadata(new ObjectMeta());
        globalConfig.getMetadata().setName("versionmanager");
        globalConfig.setSpec(new GlobalConfigSpec(ImmutableMap.of("ExpectedVersion", defaultSidecarVersion)));

        configManager.updateConfig(ImmutableList.of(globalConfig), clusterId);
    }

    @Override
    public String getLogs(String clusterId, String namespace, String podName, String container, Integer tailLines, Long sinceSeconds) {
        return configStore.getPodLog(clusterId, podName, namespace, container, tailLines, sinceSeconds);
    }

    @Override
    public void changeIstioVersion(String clusterId, String namespace, String type, String version) {
        if (!ISTIO_ENV.equals(type) && !ISTIO_REV.equals(type)) {
            throw new IllegalArgumentException("istio vertion type must be " + ISTIO_ENV + " or " + ISTIO_REV);
        }
        version = Strings.emptyToNull(version);
        Map<String, String> labels = new HashMap<>();
        labels.put(ISTIO_REV, null);
        labels.put(ISTIO_ENV, null);
        labels.put("istio-ns-scope", null);
        labels.put("istio-injection", null);
        labels.put(type, version);
        k8sResource.updateNamespaceLabel(clusterId, namespace, labels);
    }

    @Override
    public List<Map<String, String>> getIstioVersionBindings(String clusterId) {
        if (StringUtils.isEmpty(clusterId)) {
            return k8sResource.getNamespaces().entrySet().stream()
                    .flatMap(entry ->
                            entry.getValue().stream().map(ns -> getNamespaceIstioVersionBinding(ns, entry.getKey()))
                    )
                    .collect(Collectors.toList());
        } else {
            return k8sResource.getNamespaces(clusterId).stream()
                    .map(ns -> getNamespaceIstioVersionBinding(ns, clusterId))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Map<String, Map<String, String>> getSyncz(String type, String version) {
        return k8sResource.getSyncz(type, version);
    }

    private Map<String, String> getNamespaceIstioVersionBinding(Namespace ns, String clusterId) {
        Map<String, String> namespace = new HashMap<>();
        Map<String, String> labels = Optional.ofNullable(ns.getMetadata().getLabels()).orElseGet(HashMap::new);
        String env = labels.get(ISTIO_ENV);
        String rev = labels.get(ISTIO_REV);
        if (env != null) {
            namespace.put("type", ISTIO_ENV);
            namespace.put("version", env);
        } else if (rev != null) {
            namespace.put("type", ISTIO_REV);
            namespace.put("version", rev);
        }
        namespace.put("cluster", clusterId);
        namespace.put("namespace", ns.getMetadata().getName());
        return namespace;
    }

    private String doGetProjectCodeByApp(String namespace, String appName, String clusterId) {
        String projectCode = null;
        List<T> serviceList = k8sResource.getServiceByClusterAndNamespace(clusterId, namespace);
        if (CollectionUtils.isEmpty(serviceList) || StringUtils.isEmpty(appName)) {
            return null;
        }
        for (T s : serviceList) {

            if (s.getMetadata().getLabels() != null
                    && appName.equals(s.getMetadata().getLabels().get(meshConfig.getAppKey()))) {
                projectCode = extractor.getResourceInfo(s, Const.RESOURCE_TARGET);
                if (projectCode != null) {
                    break;
                }
            }

        }
        return projectCode;

    }


    private void createAppOnServiceByClusterId(String clusterId, String namespace, String name, String appName) {
        T service = (T) k8sResource.getResource(clusterId, K8sResourceEnum.Service.name(), namespace, name);
        if (service == null) {
            throw new ApiPlaneException(ExceptionConst.K8S_SERVICE_NON_EXIST, 404);
        }
        if (service.getMetadata().getLabels() == null) {
            service.getMetadata().setLabels(new HashMap<>());
        }
        if (!StringUtils.isEmpty(appName) && appName.equals(service.getMetadata().getLabels().get(meshConfig.getAppKey()))) {
            return;
        }
        service.getMetadata().getLabels().put(meshConfig.getAppKey(), appName);
        updateResource(service, clusterId);
    }

    private void updateResource(T injectedWorkLoad, String clusterId) {
        KubernetesClient kubernetesClient = multiClusterK8sClient.k8sClient(clusterId);
        try {
            kubernetesClient.createOrUpdate(injectedWorkLoad, OBJECT);
        } catch (ApiPlaneException e) {
            // 对新老版本k8s apiVersion 不一致的情况进行适配
            if (isInvalidApiVersion(e.getMessage())) {
                logger.warn(e.getMessage());
                injectedWorkLoad.setApiVersion("extensions/v1beta1");
                kubernetesClient.createOrUpdate(injectedWorkLoad, OBJECT);
            } else {
                logger.error("sidecar inject error", e);
            }
        }
    }


    private String getDefaultClusterId() {
        //TODO 待multiClusterClient暴露默认集群字段，目前使用hack方式
        return "default";
    }


    class App2ProjectIndex {

        private String appName;
        private String namespace;
        private String clusterId;

        public App2ProjectIndex(String appName, String namespace, String clusterId) {
            this.appName = appName;
            this.namespace = namespace;
            this.clusterId = clusterId;
        }

        public String getClusterId() {
            return clusterId;
        }

        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            App2ProjectIndex that = (App2ProjectIndex) o;
            return Objects.equals(appName, that.appName) &&
                    Objects.equals(namespace, that.namespace) &&
                    Objects.equals(clusterId, that.clusterId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(appName, namespace, clusterId);
        }
    }
}
