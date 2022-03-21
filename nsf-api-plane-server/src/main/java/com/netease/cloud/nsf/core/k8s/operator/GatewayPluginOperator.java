package com.netease.cloud.nsf.core.k8s.operator;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.proto.k8s.K8sTypes;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import slime.microservice.plugin.v1alpha1.EnvoyPluginOuterClass;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/13
 **/
@Component
public class GatewayPluginOperator implements k8sResourceOperator<K8sTypes.EnvoyPlugin> {

    @Override
    public K8sTypes.EnvoyPlugin merge(K8sTypes.EnvoyPlugin old, K8sTypes.EnvoyPlugin fresh) {

        K8sTypes.EnvoyPlugin latest = new K8sTypes.EnvoyPlugin();
        latest.setKind(old.getKind());
        latest.setApiVersion(old.getApiVersion());
        latest.setMetadata(old.getMetadata());
        if (fresh.getMetadata() != null && fresh.getMetadata().getLabels() != null){
            latest.getMetadata().setLabels(fresh.getMetadata().getLabels());
        }

        EnvoyPluginOuterClass.EnvoyPlugin.Builder builder = old.getSpec().toBuilder();
        EnvoyPluginOuterClass.EnvoyPlugin freshSpec = fresh.getSpec();
        if (freshSpec.getPluginsCount() > 0){
            builder.clearPlugins();
            builder.addAllPlugins(freshSpec.getPluginsList());
        }
        if (freshSpec.getHostCount() > 0){
            builder.clearHost();
            builder.addAllHost(freshSpec.getHostList());
        }
        if (freshSpec.getGatewayCount() > 0){
            builder.clearGateway();
            builder.addAllGateway(freshSpec.getGatewayList());
        }
        if (freshSpec.getRouteCount() > 0){
            builder.clearRoute();
            builder.addAllRoute(freshSpec.getRouteList());
        }
        if (freshSpec.getServiceCount() > 0){
            builder.clearService();
            builder.addAllService(freshSpec.getServiceList());
        }
        latest.setSpec(builder.build());
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
