package com.netease.cloud.nsf.core.servicemesh;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.core.AbstractConfigManagerSupport;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.K8sResourcePack;
import com.netease.cloud.nsf.core.k8s.event.NotifyRateLimitServerEvent;
import com.netease.cloud.nsf.core.k8s.event.RlsInfo;
import com.netease.cloud.nsf.core.k8s.operator.VersionManagerOperator;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.service.PluginService;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.networking.v1alpha3.Sidecar;
import me.snowdrop.istio.api.networking.v1alpha3.VersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
@Component
public class K8sServiceMeshConfigManager extends AbstractConfigManagerSupport implements ServiceMeshConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(K8sServiceMeshConfigManager.class);

    ServiceMeshIstioModelEngine modelEngine;
    MultiK8sConfigStore multiK8sConfigStore;
    PluginService pluginService;
    ApplicationEventPublisher eventPublisher;

    private static final String VM_RESOURCE_NAME = "version-manager";

    LoadingCache<ResourceMeta, Set<String>> sidecarCache;
    private Object sidecarRefreshLock = new Object();

    @Value("${rlsNamespace:istio-system}")
    String rlsNamespace;

    @Value("${rlsApp:rate-limit}")
    String rlsApp;

    @Autowired
    public K8sServiceMeshConfigManager(ServiceMeshIstioModelEngine modelEngine, MultiK8sConfigStore multiK8sConfigStore, PluginService pluginService, ApplicationEventPublisher eventPublisher) {
        this.modelEngine = modelEngine;
        this.multiK8sConfigStore = multiK8sConfigStore;
        this.pluginService = pluginService;
        this.eventPublisher = eventPublisher;
        initSidecarCache();
    }

    @Override
    public void updateConfig(List<HasMetadata> resources, String clusterId) {
        if (Objects.isNull(resources)) resources = Collections.emptyList();
        List<K8sResourcePack> packs = resources.stream().map(K8sResourcePack::new).collect(Collectors.toList());
        update(multiK8sConfigStore.resolve(clusterId), packs, modelEngine);
    }

    @Override
    public void updateConfig(SidecarVersionManagement svm, String clusterId) {
        List<K8sResourcePack> resources = modelEngine.translate(svm);
        update(multiK8sConfigStore.resolve(clusterId), resources, modelEngine);
    }

    @Override
    public List<PodStatus> querySVMConfig(PodVersion podVersion, String clusterId) {
        HasMetadata versionmanager = multiK8sConfigStore.get(K8sResourceEnum.VersionManager.name(), podVersion.getNamespace(), VM_RESOURCE_NAME, clusterId);
        if (versionmanager == null) {
            return null;
        }
        VersionManagerOperator ir = (VersionManagerOperator) modelEngine.getOperator().resolve(versionmanager);
        return ir.getPodVersion(podVersion, (VersionManager) versionmanager);
    }

    @Override
    public String querySVMExpectedVersion(String clusterId, String namespace, String workLoadType, String workLoadName) {
        HasMetadata versionmanager = multiK8sConfigStore.get(K8sResourceEnum.VersionManager.name(), namespace, VM_RESOURCE_NAME, clusterId);
        if(versionmanager == null) {
            return null;
        }
        VersionManagerOperator ir = (VersionManagerOperator) modelEngine.getOperator().resolve(versionmanager);
        return ir.getExpectedVersion((VersionManager)versionmanager, workLoadType, workLoadName);
    }

    @Override
    public IptablesConfig queryIptablesConfigByApp(String clusterId, String namespace, String appName) {
        HasMetadata versionmanager = multiK8sConfigStore.get(K8sResourceEnum.VersionManager.name(), namespace, VM_RESOURCE_NAME, clusterId);
        if (versionmanager == null) {
            return null;
        }
        VersionManagerOperator ir = (VersionManagerOperator) modelEngine.getOperator().resolve(versionmanager);
        return ir.getIptablesConfigOfApp((VersionManager) versionmanager, appName);
    }

    @Override
    public void updateRateLimit(ServiceMeshRateLimit rateLimit) {
        List<K8sResourcePack> packs = modelEngine.translate(rateLimit);
        update(multiK8sConfigStore, packs, modelEngine);
        notifyRateLimitServer(rateLimit);
    }


    private void notifyRateLimitServer(ServiceMeshRateLimit rateLimit) {
        String clusterId = StringUtils.isEmpty(rateLimit.getClusterId()) ? multiK8sConfigStore.getDefaultClusterId() : rateLimit.getClusterId();
        RlsInfo rlsInfo = new RlsInfo(ImmutableMap.of("app", rlsApp), clusterId,
                rlsNamespace, "configmap/version", System.currentTimeMillis() + "");
        NotifyRateLimitServerEvent notifyRateLimitServerEvent = new NotifyRateLimitServerEvent(rlsInfo);
        eventPublisher.publishEvent(notifyRateLimitServerEvent);
    }

    @Override
    public void updateCircuitBreaker(ServiceMeshCircuitBreaker circuitBreaker) {
        List<K8sResourcePack> packs = modelEngine.translate(circuitBreaker);
        update(multiK8sConfigStore, packs, modelEngine);
    }

    @Override
    public void deleteRateLimit(ServiceMeshRateLimit rateLimit) {
        List<K8sResourcePack> packs = modelEngine.translate(rateLimit);
        delete(multiK8sConfigStore, packs, modelEngine);
        notifyRateLimitServer(rateLimit);
    }

    @Override
    public void updateSidecarScope(String sourceService, String sourceNamespace, String targetService) {

        ResourceMeta meta = new ResourceMeta(sourceService, sourceNamespace);
        Set<String> hosts = Collections.EMPTY_SET;
        try {
            hosts = sidecarCache.get(meta);
        } catch (ExecutionException e) {
            logger.warn("get sidecar from cache failed", e);
        }
        if (hosts.contains(buildRule(targetService))) return;

        synchronized (sidecarRefreshLock) {
            List<K8sResourcePack> packs = modelEngine.translateSidecar(sourceService, sourceNamespace, targetService);
            Sidecar latestSidecar = getLatestSidecar(meta);
            Sidecar generatedSidecar = (Sidecar) packs.stream().findFirst().get().getResource();
            Sidecar finalSidecar = generatedSidecar;
            if (latestSidecar != null) {
                //已经包含，跳过
                if (containsHosts(latestSidecar, generatedSidecar)) return;
                finalSidecar = (Sidecar) modelEngine.merge(latestSidecar, generatedSidecar);
            }
            multiK8sConfigStore.update(finalSidecar);
            //update cache
            sidecarCache.put(meta, extractHosts(finalSidecar));
        }
    }

    private boolean containsHosts(Sidecar latestSidecar, Sidecar generatedSidecar) {

        Set<String> latestHosts = extractHosts(latestSidecar);
        Set<String> generatedHosts = extractHosts(generatedSidecar);

        return latestHosts.containsAll(generatedHosts);
    }

    private void initSidecarCache() {

        sidecarCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(2, TimeUnit.HOURS)
                .build(new CacheLoader<ResourceMeta, Set<String>>() {
                    @Override
                    public Set<String> load(ResourceMeta meta) throws Exception {
                        return getLatestHosts(meta);
                    }
                });

    }

    private Set<String> getLatestHosts(ResourceMeta meta) {
        Sidecar sidecar = getLatestSidecar(meta);
        return extractHosts(sidecar);
    }

    private Sidecar getLatestSidecar(ResourceMeta meta) {
        return (Sidecar) multiK8sConfigStore.get(K8sResourceEnum.Sidecar.name(), meta.namespace, meta.name);
    }

    private Set<String> extractHosts(Sidecar sidecar) {
        if (sidecar == null || sidecar.getSpec() == null ||
            sidecar.getSpec().getEgress() == null || sidecar.getSpec().getEgress().get(0) == null ||
                CollectionUtils.isEmpty(sidecar.getSpec().getEgress().get(0).getHosts())) return Collections.EMPTY_SET;
        return new HashSet<>(sidecar.getSpec().getEgress().get(0).getHosts());
    }

    private String buildRule(String target) {
        return "*/" + target;
    }

    @Override
    protected void deleteNotification(HasMetadata i) {
        // do nothing
    }

    class ResourceMeta {

        String name;
        String namespace;

        public ResourceMeta(String name, String namespace) {
            this.name = name;
            this.namespace = namespace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResourceMeta that = (ResourceMeta) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(namespace, that.namespace);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, namespace);
        }
    }

    public void setRlsNamespace(String rlsNamespace) {
        this.rlsNamespace = rlsNamespace;
    }

    public void setRlsApp(String rlsApp) {
        this.rlsApp = rlsApp;
    }
}
