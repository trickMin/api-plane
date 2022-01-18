package com.netease.cloud.nsf.core.k8s.empty;

import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;

import java.util.function.Supplier;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/23
 **/
public class DynamicGatewayPluginSupplier implements Supplier<GatewayPlugin> {

    private String gw;
    private String name;
    private String format;

    public DynamicGatewayPluginSupplier(String gw, String name, String format) {
        this.gw = gw;
        this.name = name;
        this.format = format;
    }

    @Override
    public GatewayPlugin get() {
        String realName = String.format(format, name, gw);
        return new EmptyGatewayPlugin(realName);
    }
}
