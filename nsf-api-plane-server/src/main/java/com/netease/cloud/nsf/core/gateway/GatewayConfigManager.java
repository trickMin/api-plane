package com.netease.cloud.nsf.core.gateway;

import com.google.common.collect.ImmutableSet;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Component
public class GatewayConfigManager implements ConfigManager {

    @Autowired
    private GatewayModelProcessor modelProcessor;

    @Autowired
    private ConfigStore configStore;

    private static final Set<String> API_REFERENCE_TYPES = ImmutableSet.of(K8sResourceEnum.VirtualService.name(), K8sResourceEnum.DestinationRule.name(),
            K8sResourceEnum.Gateway.name());

    @Override
    public void updateConfig(API api, String namespace) {
        List<IstioResource> resources = modelProcessor.translate(api, namespace);
        for (IstioResource latest : resources) {
            IstioResource old = configStore.get(latest);
            if (old != null) {
                configStore.update(modelProcessor.merge(old, latest));
            }
            configStore.update(latest);
        }
    }

    @Override
    public void deleteConfig(API api, String namespace) {
        List<IstioResource> resources = modelProcessor.translate(api, namespace);
        List<IstioResource> existResources = new ArrayList<>();
        for (IstioResource resource : resources) {
            IstioResource exist = configStore.get(resource);
            if (exist != null) {
                existResources.add(exist);
            }
        }
        if (CollectionUtils.isEmpty(existResources)) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        existResources.stream()
                .map(er -> modelProcessor.subtract(er, api.getService(), api.getName()))
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

    @Override
    public List<IstioResource> getConfigResources(String service, String namespace) {

        return API_REFERENCE_TYPES.stream()
                    .map(kind -> configStore.get(kind, namespace, service))
                    .filter(i -> i != null)
                    .collect(Collectors.toList());
    }

}
