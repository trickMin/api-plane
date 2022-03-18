package com.netease.cloud.nsf.core.k8s.operator;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.proto.k8s.K8sTypes;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPluginBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.Plugins;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/13
 **/
@Component
public class GatewayPluginOperator implements k8sResourceOperator<K8sTypes.EnvoyPlugin> {

    @Override
    public K8sTypes.EnvoyPlugin merge(K8sTypes.EnvoyPlugin old, K8sTypes.EnvoyPlugin fresh) {

        K8sTypes.EnvoyPlugin latest = new K8sTypes.EnvoyPlugin();

        List<Plugins> latestPlugins = fresh.getSpec().getPlugins();
        latest.getSpec().setPlugins(latestPlugins);
        latest.getSpec().setHost(fresh.getSpec().getHost());
        latest.getSpec().setGateway(fresh.getSpec().getGateway());
        latest.getSpec().setRoute(fresh.getSpec().getRoute());
        latest.getSpec().setService(fresh.getSpec().getService());
        if (fresh.getMetadata() != null && fresh.getMetadata().getLabels() != null) {
            latest.getMetadata().setLabels(fresh.getMetadata().getLabels());
        }
        return latest;
    }

    @Override
    public K8sTypes.EnvoyPlugin subtract(K8sTypes.EnvoyPlugin old, String value) {
        old.setSpec(null);
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.EnvoyPlugin.name().equals(name);
    }

    @Override
    public boolean isUseless(K8sTypes.EnvoyPlugin gp) {
        return gp == null ||
                StringUtils.isEmpty(gp.getApiVersion()) ||
                 gp.getSpec() == null ||
                  CollectionUtils.isEmpty(gp.getSpec().getPluginsList());
    }
}
