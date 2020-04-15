package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.core.k8s.K8sConst;
import com.netease.cloud.nsf.util.function.Subtracter;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;
import me.snowdrop.istio.api.networking.v1alpha3.Plugins;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/13
 **/
public class MeshRateLimitGatewayPluginSubtracter implements Subtracter<GatewayPlugin> {

    @Override
    public GatewayPlugin subtract(GatewayPlugin gatewayPlugin) {

        if (gatewayPlugin == null ||
                gatewayPlugin.getSpec() == null ||
                CollectionUtils.isEmpty(gatewayPlugin.getSpec().getPlugins())) return gatewayPlugin;

        List<Plugins> filteredPlugins = gatewayPlugin.getSpec().getPlugins().stream()
                .filter(p -> !p.getName().equals(K8sConst.RATE_LIMIT_PLUGIN_NAME))
                .collect(Collectors.toList());
        gatewayPlugin.getSpec().setPlugins(filteredPlugins);
        return gatewayPlugin;
    }
}
