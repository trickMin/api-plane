package com.netease.cloud.nsf.core.k8s.operator;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/13
 **/
@Component
public class GatewayPluginOperator implements k8sResourceOperator<GatewayPlugin> {

    @Override
    public GatewayPlugin merge(GatewayPlugin old, GatewayPlugin fresh) {

        GatewayPlugin latest = new GatewayPluginBuilder(old).build();

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
    public GatewayPlugin subtract(GatewayPlugin old, String value) {
        old.setSpec(null);
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.GatewayPlugin.name().equals(name);
    }

    @Override
    public boolean isUseless(GatewayPlugin gp) {
        return gp == null ||
                StringUtils.isEmpty(gp.getApiVersion()) ||
                 gp.getSpec() == null ||
                  CollectionUtils.isEmpty(gp.getSpec().getPlugins());
    }
}
