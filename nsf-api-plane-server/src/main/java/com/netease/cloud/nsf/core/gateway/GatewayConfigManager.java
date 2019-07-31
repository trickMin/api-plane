package com.netease.cloud.nsf.core.gateway;

import com.google.common.collect.ImmutableSet;
import com.netease.cloud.nsf.meta.APIModel;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.meta.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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

    @Value("{apiNamespace:gateway-config}")
    private String apiNamespace;

    /**
     * api目前只对应virtualservice和destinationrule两个资源
     */
    private static final Set<String> API_REFERENCE_TYPES = ImmutableSet.of(K8sResourceEnum.VirtualService.name(), K8sResourceEnum.DestinationRule.name(),
            K8sResourceEnum.Gateway.name());

    @Override
    public void updateConfig(APIModel api) {

        // TODO clean up old resources first

        List<IstioResource> resources = modelProcessor.translate(api, apiNamespace);
        for (IstioResource latest : resources) {
            IstioResource old = configStore.get(latest);
            if (old != null) {
                configStore.update(modelProcessor.merge(old, latest));
            }
            configStore.update(latest);
        }
    }

    @Override
    public void deleteConfig(String service, String name) {
        List<IstioResource> existResource = getConfigResources(service);
        if (CollectionUtils.isEmpty(existResource)) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        existResource.stream()
                .map(er -> modelProcessor.subtract(er, name))
                .filter(i -> i != null)
                .forEach(r -> configStore.update(r));
    }

    @Override
    public List<IstioResource> getConfigResources(String service) {

        return API_REFERENCE_TYPES.stream()
                    .map(kind -> configStore.get(kind, apiNamespace, service))
                    .filter(i -> i != null)
                    .collect(Collectors.toList());
    }


}
