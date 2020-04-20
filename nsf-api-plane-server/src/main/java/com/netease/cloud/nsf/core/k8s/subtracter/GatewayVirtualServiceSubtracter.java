package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.util.function.Subtracter;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/17
 **/
public class GatewayVirtualServiceSubtracter implements Subtracter<VirtualService> {

    private String key;

    public GatewayVirtualServiceSubtracter(String key) {
        this.key = key;
    }

    @Override
    public VirtualService subtract(VirtualService old) {
        List<HTTPRoute> latestHttp = old.getSpec().getHttp().stream()
                .filter(h -> !h.getApi().equals(key))
                .collect(Collectors.toList());

        old.getSpec().setHttp(latestHttp);
        return old;
    }
}
