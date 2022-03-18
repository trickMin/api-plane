package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.proto.k8s.K8sTypes;
import com.netease.cloud.nsf.util.function.Subtracter;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/23
 **/
public class GatewayPluginNormalSubtracter implements Subtracter<K8sTypes.EnvoyPlugin> {
    @Override
    public K8sTypes.EnvoyPlugin subtract(K8sTypes.EnvoyPlugin gatewayPlugin) {
        gatewayPlugin.setSpec(null);
        return gatewayPlugin;
    }
}
