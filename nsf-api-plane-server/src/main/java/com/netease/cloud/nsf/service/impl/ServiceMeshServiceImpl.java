package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.cache.K8sResourceCache;
import com.netease.cloud.nsf.cache.ResourceStoreFactory;
import com.netease.cloud.nsf.cache.meta.PodDTO;
import com.netease.cloud.nsf.configuration.ApiPlaneConfig;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.service.impl.MultiK8sConfigStore;
import com.netease.cloud.nsf.core.istio.PilotHttpClient;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import com.netease.cloud.nsf.meta.SVMSpec;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import com.netease.cloud.nsf.meta.dto.ResourceWrapperDTO;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.service.ServiceMeshService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.RestTemplateClient;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import me.snowdrop.istio.api.IstioResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.netease.cloud.nsf.core.editor.ResourceType.OBJECT;
import static com.netease.cloud.nsf.core.k8s.K8sResourceEnum.DaemonSet;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/7
 **/
@Service
public class ServiceMeshServiceImpl<T extends HasMetadata> implements ServiceMeshService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceMeshServiceImpl.class);
    private static final String DEFAULT_SIDECAR_VERSION = "envoy";
    private ExecutorService taskPool = Executors.newCachedThreadPool();

    @Autowired
    MultiK8sConfigStore configStore;

    @Autowired
    K8sResourceCache k8sResource;

    @Autowired
    KubernetesClient httpClient;

    @Autowired
    GatewayService gatewayService;

    @Autowired
    RestTemplateClient restTemplateClient;

    @Autowired
    ApiPlaneConfig apiPlaneConfig;

    @Autowired
    PilotHttpClient pilotHttpClient;

    @Autowired
    private MultiClusterK8sClient multiClusterK8sClient;

    @Override
    public void updateIstioResource(String json) {

        json = optimize(json);
        configStore.update(json2Resource(json));
    }

    @Override
    public void deleteIstioResource(String json) {

        json = optimize(json);
        configStore.delete(json2Resource(json));
    }

    @Override
    public List<ResourceWrapperDTO> getIstioResourceList(String namespaces, String kind) {

        if (StringUtils.isEmpty(namespaces)) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST, 404);
        List<ResourceWrapperDTO> wrapperDTOS = new ArrayList<>();
        String defaultClusterId = getDefaultClusterId();

        for (String ns : namespaces.split(",")) {
            if (StringUtils.isEmpty(ns)) continue;
            List<HasMetadata> resources;
            try {
                resources = configStore.getList(kind, ns, defaultClusterId);
            } catch (Exception e) {
                logger.warn("find resources failed", e);
                continue;
            }
            for (HasMetadata r : resources) {
                wrapperDTOS.add(new ResourceWrapperDTO(r, defaultClusterId));
            }
        }
        return wrapperDTOS;
    }

    @Override
    public HasMetadata getIstioResource(String name, String namespace, String kind) {

        String defaultClusterId = getDefaultClusterId();
        HasMetadata resource = configStore.get(name, kind, namespace, defaultClusterId);
        if (resource == null) {
            throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST, 404);
        }
        return resource;
    }

    @Override
    public ErrorCode sidecarInject(String clusterId, String kind, String namespace, String name, String version, String expectedVersion) {
        if (!K8sResourceEnum.StatefulSet.name().equals(kind) && !K8sResourceEnum.Deployment.name().equals(kind)) {
            return ApiPlaneErrorCode.MissingParamsError("resource kind");
        }
        T resourceToInject = (T) k8sResource.getResource(clusterId, kind, namespace, name);
        if (resourceToInject == null) {
            return ApiPlaneErrorCode.workLoadNotFound;
        }
        if (!checkEnable(namespace)) {
            return ApiPlaneErrorCode.sidecarInjectPolicyError;
        }
        Map<String, String> versionLabel = new HashMap<>(1);
        Map<String, String> injectAnnotation = new HashMap<>(1);
        versionLabel.put(Const.LABEL_NSF_VERSION, version);
        injectAnnotation.put(Const.ISTIO_INJECT_ANNOTATION, "true");
        T injectedWorkLoad = appendLabel(appendAnnotationToPod(resourceToInject, injectAnnotation), versionLabel);
        try {
            httpClient.createOrUpdate(injectedWorkLoad, OBJECT);
        } catch (ApiPlaneException e) {
            // 对新老版本k8s apiVersion 不一致的情况进行适配
            if (isInvalidApiVersion(e.getMessage())) {
                logger.warn(e.getMessage());
                injectedWorkLoad.setApiVersion("extensions/v1beta1");
                httpClient.createOrUpdate(injectedWorkLoad, OBJECT);
            } else {
                logger.error("sidecar inject error", e);
            }
        }
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

    private boolean checkEnable(String namespace) {
        String url = httpClient.getUrl("Namespace", namespace);
        Namespace ns = httpClient.getObject(url);
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
        gatewayService.updateSVM(versionManagement);
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


    private String getDefaultClusterId() {
        Map<String, MultiClusterK8sClient.ClientSet> allClients = multiClusterK8sClient.getAllClients();
//        allClients.
        //TODO 待multiClusterClient暴露默认集群字段，目前使用hack方式
        return "default";
    }
}
