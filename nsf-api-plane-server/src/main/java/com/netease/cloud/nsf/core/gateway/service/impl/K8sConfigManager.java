package com.netease.cloud.nsf.core.gateway.service.impl;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.core.editor.PathExpressionEnum;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.IstioModelEngine;
import com.netease.cloud.nsf.core.gateway.service.ConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ConfigStore;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.K8sResourcePack;
import com.netease.cloud.nsf.core.k8s.operator.VersionManagerOperator;
import com.netease.cloud.nsf.core.k8s.operator.k8sResourceOperator;
import com.netease.cloud.nsf.core.k8s.subtracter.ServiceEntryEndpointsSubtracter;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.service.ValidateService;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import com.netease.cloud.nsf.util.function.Subtracter;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Component
public class K8sConfigManager implements ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(K8sConfigManager.class);

    private static final String VM_RESOURCE_NAME = "version-manager";

    @Autowired
    private IstioModelEngine modelProcessor;

    @Autowired
    private K8sConfigStore configStore;

    @Autowired
    private MultiK8sConfigStore multiK8sConfigStore;

    @Autowired
    private List<k8sResourceOperator> operators;

    @Autowired
    private ValidateService validateService;

    @Override
    public void updateConfig(API api) {
        List<K8sResourcePack> resources = modelProcessor.translate(api);
        update(resources);
    }

    @Override
    public void updateConfig(Service service) {
        List<K8sResourcePack> resources = modelProcessor.translate(service);
        update(resources);
    }

    private void update(List<K8sResourcePack> resources) {
        update(resources, null);
    }

    private boolean isDefault(String clusterId) {
        if (StringUtils.isEmpty(clusterId) || clusterId.equals("default")) return true;
        return false;
    }

    private void update(ConfigStore configStore, List<K8sResourcePack> resources) {
        if (CollectionUtils.isEmpty(resources)) return;

        for (K8sResourcePack pack : resources) {
            HasMetadata latest = pack.getResource();
            HasMetadata old = configStore.get(latest);
            if (old != null) {
                if (old.equals(latest)) continue;
                HasMetadata merged;
                if (pack.hasMerger()) {
                    merged = pack.getMerger().merge(old, latest);
                } else {
                    merged = modelProcessor.merge(old, latest);
                }
                if (merged != null) {
                    configStore.update(merged);
                }
                continue;
            }
            configStore.update(latest);
        }
    }

    private void update(List<K8sResourcePack> resources, String clusterId) {
        update(isDefault(clusterId) ? configStore : multiK8sConfigStore, resources);
    }

    @Override
    public void deleteConfig(API api) {
        List<K8sResourcePack> resources = modelProcessor.translate(api, true);

        ImmutableMap<String, String> toBeDeletedMap = ImmutableMap
                .of(K8sResourceEnum.VirtualService.name(), api.getName(),
                        K8sResourceEnum.DestinationRule.name(), String.format("%s-%s", api.getService(), api.getName()));

        delete(resources, resource -> modelProcessor.subtract(resource, toBeDeletedMap));
    }

    @Override
    public void deleteConfig(Service service) {
        if (StringUtils.isEmpty(service.getGateway())) return;
        List<K8sResourcePack> resources = modelProcessor.translate(service);
        Class<? extends HasMetadata> drClz = K8sResourceEnum.DestinationRule.mappingType();
        Class<? extends HasMetadata> seClz = K8sResourceEnum.ServiceEntry.mappingType();

        //move dr to head
        Comparator<K8sResourcePack> compareFun = (p1, p2) -> {
            if (p1.getResource().getClass() == drClz) {
                return -1;
            } else if (p2.getResource().getClass() == drClz) {
                return 1;
            }
            return 0;
        };

        Set<String> subsets = new HashSet<>();
        if (!CollectionUtils.isEmpty(service.getSubsets())) {
            subsets.addAll(service.getSubsets().stream().map(s -> s.getName()).collect(Collectors.toSet()));
        }
        Subtracter<HasMetadata> deleteFun = new Subtracter<HasMetadata>() {
            //dr若不存在subset，则全部删除相关资源
            boolean noSubsets = false;

            @Override
            public HasMetadata subtract(HasMetadata r) {
                if (r.getClass() == drClz) {
                    ResourceGenerator gen = ResourceGenerator.newInstance(r, ResourceType.OBJECT);
                    // 如果没有传入subset，则删除默认subset
                    if (CollectionUtils.isEmpty(subsets)) {
                        subsets.add(String.format("%s-%s", service.getCode(), service.getGateway()));
                    }
                    subsets.forEach(ss -> gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET_NAME.translate(ss)));
                    DestinationRule dr = gen.object(DestinationRule.class);
                    if (CollectionUtils.isEmpty(dr.getSpec().getSubsets())) noSubsets = true;
                    return dr;
                } else if (r.getClass() == seClz){
                    // 若没有subset，则删除整个se
                    if (noSubsets) {
                        r.setApiVersion(null);
                    } else {
                        ServiceEntryEndpointsSubtracter sub = new ServiceEntryEndpointsSubtracter(service.getGateway());
                        return sub.subtract((ServiceEntry) r);
                    }
                } else {
                    if (noSubsets) r.setApiVersion(null);
                }
                return r;
            }
        };
        delete(resources, compareFun, deleteFun);
    }

    @Override
    public HasMetadata getConfig(PluginOrder pluginOrder) {
        List<K8sResourcePack> resources = modelProcessor.translate(pluginOrder);
        if (CollectionUtils.isEmpty(resources) || resources.size() != 1) throw new ApiPlaneException();
        return configStore.get(resources.get(0).getResource());
    }

    @Override
    public void updateConfig(PluginOrder pluginOrder) {
        List<K8sResourcePack> resources = modelProcessor.translate(pluginOrder);
        update(resources);
    }

    @Override
    public void deleteConfig(PluginOrder pluginOrder) {
        List<K8sResourcePack> resources = modelProcessor.translate(pluginOrder);
        delete(resources, clearResource());
    }

    @Override
    public void updateConfig(SidecarVersionManagement svm) {
        List<K8sResourcePack> resources = modelProcessor.translate(svm);
        update(resources, svm.getClusterId());
    }

    @Override
    public List<PodStatus> querySVMConfig(PodVersion podVersion) {
        String clusterId = podVersion.getClusterId();
        HasMetadata versionmanager = multiK8sConfigStore.get(K8sResourceEnum.VersionManager.name(), podVersion.getNamespace(), VM_RESOURCE_NAME, clusterId);
        if (versionmanager == null) {
            return null;
        }
        VersionManagerOperator ir = (VersionManagerOperator) resolve(versionmanager);
        return ir.getPodVersion(podVersion, (VersionManager) versionmanager);
    }

    @Override
    public String querySVMExpectedVersion(String clusterId, String namespace, String workLoadType, String workLoadName) {
        HasMetadata versionmanager = multiK8sConfigStore.get(K8sResourceEnum.VersionManager.name(), namespace, VM_RESOURCE_NAME, clusterId);
        if(versionmanager == null) {
            return null;
        }
        VersionManagerOperator ir = (VersionManagerOperator)resolve(versionmanager);
        return ir.getExpectedVersion((VersionManager)versionmanager, workLoadType, workLoadName);
    }

    @Override
    public HasMetadata getConfig(IstioGateway istioGateway) {
        if (StringUtils.isEmpty(istioGateway.getGwCluster())) {
            return null;
        }
        final String gwClusgterKey = "gw_cluster";
        Gateway gwt = new Gateway();
        gwt.setMetadata(new ObjectMeta());
        configStore.supply(gwt);
        ObjectMeta metadata = gwt.getMetadata();
        //获取所有Gateway资源
        List<HasMetadata> istioResources = configStore.get(gwt.getKind(), metadata.getNamespace());
        Optional<HasMetadata> first = istioResources.stream().filter(g ->
        {
            IstioResource ir = (IstioResource) g;
            GatewaySpec spec = (GatewaySpec) ir.getSpec();
            if (spec == null) {
                return false;
            }
            Map<String, String> selector = spec.getSelector();
            if (selector == null) {
                return false;
            }
            return istioGateway.getGwCluster().equals(selector.get(gwClusgterKey));
        }).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return null;
    }

    @Override
    public void updateConfig(IstioGateway istioGateway) {
        List<K8sResourcePack> resources = modelProcessor.translate(istioGateway);
        update(resources);
    }

    @Override
    public void updateConfig(GlobalPlugin gp) {
        List<K8sResourcePack> resources = modelProcessor.translate(gp);
        update(resources);
    }

    @Override
    public void deleteConfig(GlobalPlugin gp) {
        List<K8sResourcePack> resources = modelProcessor.translate(gp);

        Subtracter<HasMetadata> deleteFun = r -> {

            if (r.getClass() == K8sResourceEnum.GatewayPlugin.mappingType()) {
                GatewayPlugin gatewayPlugin = (GatewayPlugin) r;
                gatewayPlugin.setSpec(null);
                return gatewayPlugin;
            } else if (r.getClass() == K8sResourceEnum.SharedConfig.mappingType()) {
                //TODO sharedConfig唯一标识未更新
                ResourceGenerator gen = ResourceGenerator.newInstance(r, ResourceType.OBJECT);
                gen.removeElement(PathExpressionEnum.REMOVE_SC_RATELIMITDESC_BY_CODE.translate(gp.getCode()));
                return gen.object(SharedConfig.class);
            }
            return r;
        };

        delete(resources, deleteFun);
    }

    private void delete(List<K8sResourcePack> resources, Subtracter<HasMetadata> fun) {
        delete(resources, (i1, i2) -> 0, fun);
    }

    private void delete(List<K8sResourcePack> packs, Comparator<K8sResourcePack> compartor, Subtracter<HasMetadata> fun) {
        if (CollectionUtils.isEmpty(packs)) return;
        List<HasMetadata> existResources = new ArrayList<>();
        for (K8sResourcePack pack : packs) {
            HasMetadata resource = pack.getResource();
            HasMetadata exist = configStore.get(resource);
            if (exist != null) {
                pack.setResource(exist);
            }
        }
        packs.stream()
                .sorted(compartor)
                .map(p -> {
                    HasMetadata resource = p.getResource();
                    if (p.hasSubtracter()) {
                        return p.getSubtracter().subtract(resource);
                    }
                    return fun.subtract(resource);
                })
                .filter(i -> i != null)
                .forEach(r -> handle(r));
    }

    private void handle(HasMetadata i) {
        if (modelProcessor.isUseless(i)) {
            try {
                configStore.delete(i);
            } catch (Exception e) {
                //ignore error
                logger.warn("", e);
            }
        } else {
            configStore.update(i);
        }
    }

    private Subtracter<HasMetadata> clearResource() {
        return resource -> {
            resource.setApiVersion(null);
            return resource;
        };
    }

    private k8sResourceOperator resolve(HasMetadata i) {
        for (k8sResourceOperator op : operators) {
            if (op.adapt(i.getKind())) {
                return op;
            }
        }
        throw new ApiPlaneException(ExceptionConst.UNSUPPORTED_RESOURCE_TYPE + ":" + i.getKind());
    }
}
