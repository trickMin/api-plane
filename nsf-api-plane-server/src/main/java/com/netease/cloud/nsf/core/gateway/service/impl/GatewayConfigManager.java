package com.netease.cloud.nsf.core.gateway.service.impl;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.core.editor.PathExpressionEnum;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.GatewayModelOperator;
import com.netease.cloud.nsf.core.gateway.service.ConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ConfigStore;
import com.netease.cloud.nsf.core.istio.operator.IstioResourceOperator;
import com.netease.cloud.nsf.core.istio.operator.VersionManagerOperator;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.GatewaySpec;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;
import me.snowdrop.istio.api.networking.v1alpha3.SharedConfig;
import me.snowdrop.istio.api.networking.v1alpha3.VersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Component
public class GatewayConfigManager implements ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(GatewayConfigManager.class);

    private static final String VM_RESOURCE_NAME = "version-manager";

    @Autowired
    private GatewayModelOperator modelProcessor;

    @Autowired
    private K8sConfigStore configStore;

    @Autowired
    private MultiK8sConfigStore multiK8sConfigStore;

    @Autowired
    private List<IstioResourceOperator> operators;

    @Override
    public void updateConfig(API api) {
        List<IstioResource> resources = modelProcessor.translate(api);
        update(resources);
    }

    @Override
    public void updateConfig(Service service) {
        List<IstioResource> resources = modelProcessor.translate(service);
        update(resources);
    }

    private void update(List<IstioResource> resources) {
        update(resources, null);
    }

    private boolean isDefault(String clusterId) {
        if (StringUtils.isEmpty(clusterId) || clusterId.equals("default")) return true;
        return false;
    }

    private void update(ConfigStore configStore, List<IstioResource> resources) {
        if (CollectionUtils.isEmpty(resources)) return;

        for (IstioResource latest : resources) {
            IstioResource old = configStore.get(latest);
            if (old != null) {
                if (old.equals(latest)) continue;
                configStore.update(modelProcessor.merge(old, latest));
                continue;
            }
            configStore.update(latest);
        }
    }

    private void update(List<IstioResource> resources, String clusterId) {
        update(isDefault(clusterId) ? configStore : multiK8sConfigStore, resources);
    }

    @Override
    public void deleteConfig(API api) {
        List<IstioResource> resources = modelProcessor.translate(api, true);

        ImmutableMap<String, String> toBeDeletedMap = ImmutableMap
                .of(K8sResourceEnum.VirtualService.name(), api.getName(),
                        K8sResourceEnum.DestinationRule.name(), String.format("%s-%s", api.getService(), api.getName(),
                                K8sResourceEnum.SharedConfig.name(), String.format("%s-%s", api.getService(), api.getName())));

        delete(resources, resource -> modelProcessor.subtract(resource, toBeDeletedMap));
    }

    @Override
    public void deleteConfig(Service service) {
        if (StringUtils.isEmpty(service.getGateway())) return;
        List<IstioResource> resources = modelProcessor.translate(service);
        Class<? extends HasMetadata> drClz = K8sResourceEnum.DestinationRule.mappingType();

        //move dr to head
        Comparator<IstioResource> compareFun = (o1, o2) -> {
            if (o1.getClass() == drClz) {
                return -1;
            } else if (o2.getClass() == drClz) {
                return 1;
            }
            return 0;
        };

        Set<String> subsets = new HashSet<>();
        if (!CollectionUtils.isEmpty(service.getSubsets())) {
            subsets.addAll(service.getSubsets().stream().map(s -> s.getName()).collect(Collectors.toSet()));
        }
        Function<IstioResource, IstioResource> deleteFun = new Function<IstioResource, IstioResource>() {
            //dr若不存在subset，则全部删除相关资源
            boolean noSubsets = false;
            @Override
            public IstioResource apply(IstioResource r) {
                if (r.getClass() == drClz){
                    ResourceGenerator gen = ResourceGenerator.newInstance(r, ResourceType.OBJECT);
                    // 如果没有传入subset，则删除默认subset
                    if (CollectionUtils.isEmpty(subsets)) {
                        subsets.add(String.format("%s-%s", service.getCode(), service.getGateway()));
                    }
                    subsets.forEach(ss -> gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET_NAME.translate(ss)));
                    DestinationRule dr = gen.object(DestinationRule.class);
                    if (CollectionUtils.isEmpty(dr.getSpec().getSubsets())) noSubsets = true;
                    return dr;
                } else {
                    // 若没有subset，则删除所有关联资源
                    if (noSubsets) {
                        r.setApiVersion(null);
                    }
                }
                return r;
            }
        };
        delete(resources, compareFun, deleteFun);
    }

    @Override
    public IstioResource getConfig(PluginOrder pluginOrder) {
        List<IstioResource> resources = modelProcessor.translate(pluginOrder);
        if (CollectionUtils.isEmpty(resources) || resources.size() != 1) throw new ApiPlaneException();
        return configStore.get(resources.get(0));
    }

    @Override
    public void updateConfig(PluginOrder pluginOrder) {
        List<IstioResource> resources = modelProcessor.translate(pluginOrder);
        update(resources);
    }

    @Override
    public void deleteConfig(PluginOrder pluginOrder) {
        List<IstioResource> resources = modelProcessor.translate(pluginOrder);
        delete(resources, clearResource());
    }

    @Override
    public void updateConfig(SidecarVersionManagement svm) {
        List<IstioResource> resources = modelProcessor.translate(svm);
        update(resources, svm.getClusterId());
    }

    @Override
    public List<PodStatus> querySVMConfig(PodVersion podVersion) {
        String clusterId = podVersion.getClusterId();
        IstioResource versionmanager = multiK8sConfigStore.get(K8sResourceEnum.VersionManager.name(), podVersion.getNamespace(), VM_RESOURCE_NAME, clusterId);
        if(versionmanager == null) {
            return null;
        }
        VersionManagerOperator ir = (VersionManagerOperator)resolve(versionmanager);
        return ir.getPodVersion(podVersion, (VersionManager)versionmanager);
    }

    @Override
    public IstioResource getConfig(IstioGateway istioGateway) {
        if (StringUtils.isEmpty(istioGateway.getGwCluster())){
            return null;
        }
        final String gwClusgterKey = "gw_cluster";
        Gateway gwt = new Gateway();
        gwt.setMetadata(new ObjectMeta());
        configStore.supply(gwt);
        ObjectMeta metadata = gwt.getMetadata();
        //获取所有Gateway资源
        List<IstioResource> istioResources = configStore.get(gwt.getKind(), metadata.getNamespace());
        Optional<IstioResource> first = istioResources.stream().filter(g ->
        {
            GatewaySpec spec = (GatewaySpec) g.getSpec();
            if (spec == null){
                return false;
            }
            Map<String, String> selector = spec.getSelector();
            if (selector == null){
                return false;
            }
            return istioGateway.getGwCluster().equals(selector.get(gwClusgterKey));
        }).findFirst();
        if (first.isPresent()){
            return first.get();
        }
        return null;
    }

    @Override
    public void updateConfig(IstioGateway istioGateway) {
        List<IstioResource> resources = modelProcessor.translate(istioGateway);
        update(resources);
    }

    @Override
    public void updateConfig(GlobalPlugins gp) {
        List<IstioResource> resources = modelProcessor.translate(gp);
        update(resources);
    }

    @Override
    public void deleteConfig(GlobalPlugins gp) {
        List<IstioResource> resources = modelProcessor.translate(gp);

        Function<IstioResource, IstioResource> deleteFun = r -> {

            if (r.getClass() == K8sResourceEnum.GatewayPlugin.mappingType()) {
                GatewayPlugin gatewayPlugin = (GatewayPlugin) r;
                gatewayPlugin.setSpec(null);
                return gatewayPlugin;
            } else if (r.getClass() == K8sResourceEnum.SharedConfig.mappingType()) {
                //TODO
                ResourceGenerator gen = ResourceGenerator.newInstance(r, ResourceType.OBJECT);
                gen.removeElement(PathExpressionEnum.REMOVE_SC_RATELIMITDESC_BY_CODE.translate(gp.getCode()));
                return gen.object(SharedConfig.class);
            }
            return r;
        };

        delete(resources, deleteFun);
    }

    private void delete(List<IstioResource> resources, Function<IstioResource, IstioResource> fun) {
        delete(resources, (i1,i2) -> 0, fun);
    }

    private void delete(List<IstioResource> resources, Comparator<IstioResource> compare, Function<IstioResource, IstioResource> fun) {
        if (CollectionUtils.isEmpty(resources)) return;
        List<IstioResource> existResources = new ArrayList<>();
        for (IstioResource resource : resources) {
            IstioResource exist = configStore.get(resource);
            if (exist != null) {
                existResources.add(exist);
            }
        }
        existResources.stream()
                .sorted(compare)
                .map(er -> fun.apply(er))
                .filter(i -> i != null)
                .forEach(r -> handle(r));
    }

    private void handle(IstioResource i) {
        if (modelProcessor.isUseless(i)) {
            configStore.delete(i);
        } else {
            configStore.update(i);
        }
    }

    private Function<IstioResource, IstioResource> clearResource() {
        return resource -> {
            resource.setApiVersion(null);
            return resource;
        };
    }

    private IstioResourceOperator resolve(IstioResource i) {
        for (IstioResourceOperator op : operators) {
            if (op.adapt(i.getKind())) {
                return op;
            }
        }
        throw new ApiPlaneException(ExceptionConst.UNSUPPORTED_RESOURCE_TYPE + ":" + i.getKind());
    }
}
