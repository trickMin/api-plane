package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.util.function.Subtracter;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/23
 **/
public class GatewayPluginNormalSubtracter implements Subtracter<GatewayPlugin> {
    @Override
    public GatewayPlugin subtract(GatewayPlugin gatewayPlugin) {
        gatewayPlugin.setSpec(null);
        return gatewayPlugin;
    }
}
