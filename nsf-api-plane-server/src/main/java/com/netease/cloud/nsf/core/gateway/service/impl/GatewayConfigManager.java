package com.netease.cloud.nsf.core.gateway.service.impl;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.core.gateway.GatewayModelOperator;
import com.netease.cloud.nsf.core.gateway.service.ConfigManager;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.PluginOrder;
import com.netease.cloud.nsf.meta.Service;
import me.snowdrop.istio.api.IstioResource;
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
    private GatewayModelOperator modelProcessor;

    @Autowired
    private K8sConfigStore configStore;

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
        delete(resources, clearResource());
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

    private Function<IstioResource, IstioResource> clearResource() {
        return resource -> {
            resource.setApiVersion(null);
            return resource;
        };
    }
}
