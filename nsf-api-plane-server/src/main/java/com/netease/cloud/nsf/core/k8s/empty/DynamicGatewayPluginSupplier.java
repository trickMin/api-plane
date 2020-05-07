package com.netease.cloud.nsf.core.k8s.empty;

import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;

import java.util.List;
import java.util.function.Supplier;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/23
 **/
public class DynamicGatewayPluginSupplier implements Supplier<GatewayPlugin> {

    private int index;
    private int limit;
    private List<String> gws;
    private String name;
    private String format;

    public DynamicGatewayPluginSupplier(List<String> gws, String name, String format) {
        this.gws = gws;
        this.name = name;
        this.format = format;
        this.limit = gws.size() - 1;
        this.index = 0;
    }

    @Override
    public GatewayPlugin get() {
        if (index > limit) throw new ApiPlaneException("out of limit, gateway plugin supplier");
        String realName = String.format(format, name, gws.get(index++));
        return new EmptyGatewayPlugin(realName);
    }
}
