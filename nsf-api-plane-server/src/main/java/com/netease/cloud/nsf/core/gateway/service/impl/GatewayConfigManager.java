package com.netease.cloud.nsf.core.gateway.service.impl;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.GatewayModelProcessor;
import com.netease.cloud.nsf.core.gateway.service.ConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ConfigStore;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.PluginOrder;
import com.netease.cloud.nsf.meta.Service;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.PluginManager;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Component
public class GatewayConfigManager implements ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(GatewayConfigManager.class);

    @Autowired
    private GatewayModelProcessor modelProcessor;

    @Autowired
    private ConfigStore configStore;

    @Override
    public void updateConfig(API api, String namespace) {
        List<IstioResource> resources = modelProcessor.translate(api, namespace);
        update(resources);
    }

    @Override
    public void updateConfig(Service service, String namespace) {
        List<IstioResource> resources = modelProcessor.translate(service, namespace);
        update(resources);
    }

    private void update(List<IstioResource> resources) {
        if (CollectionUtils.isEmpty(resources)) return;

        //TODO rollback mechanism
        for (IstioResource latest : resources) {
            IstioResource old = configStore.get(latest);
            if (old != null) {
                configStore.update(modelProcessor.merge(old, latest));
                continue;
            }
            configStore.update(latest);
        }
    }

    @Override
    public void deleteConfig(API api, String namespace) {
        List<IstioResource> resources = modelProcessor.translate(api, namespace);
        delete(resources, resource -> modelProcessor.subtract(resource, api.getService(), api.getName()));
    }

    @Override
    public void deleteConfig(Service service, String namespace) {
        if (StringUtils.isEmpty(service.getGateway())) return;
        List<IstioResource> resources = modelProcessor.translate(service, namespace);
        delete(resources, r -> {
            if (r.getKind().equals(K8sResourceEnum.DestinationRule.name())) {
                DestinationRule ds = (DestinationRule) r;
                ResourceGenerator gen = ResourceGenerator.newInstance(ds, ResourceType.OBJECT);
                gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET_NAME
                                .translate(service.getCode() + "-" + service.getGateway()));
                return gen.object(DestinationRule.class);
            } else if (r.getKind().equals(K8sResourceEnum.ServiceEntry)) {
                ServiceEntry se = (ServiceEntry) r;
                se.setSpec(null);
                return se;
            }
            throw new ApiPlaneException("Unsupported operation");
        });
    }

    @Override
    public void updateConfig(PluginOrder pluginOrder, String namespace) {
        List<IstioResource> resources = modelProcessor.translate(pluginOrder, namespace);
        update(resources);
    }

    @Override
    public void deleteConfig(PluginOrder pluginOrder, String namespace) {
        List<IstioResource> resources = modelProcessor.translate(pluginOrder, namespace);
        delete(resources, r -> {
            if (r.getKind().equals(K8sResourceEnum.PluginManager.name())) {
                PluginManager pm = (PluginManager) r;
                pm.setSpec(null);
                return pm;
            }
            throw new ApiPlaneException("Unsupported operation");
        });
    }

    private void delete(List<IstioResource> resources, Function<IstioResource, IstioResource> fun) {
        if (CollectionUtils.isEmpty(resources)) return;
        List<IstioResource> existResources = new ArrayList<>();
        for (IstioResource resource : resources) {
            IstioResource exist = configStore.get(resource);
            if (exist != null) {
                existResources.add(exist);
            }
        }
        existResources.stream()
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

}
