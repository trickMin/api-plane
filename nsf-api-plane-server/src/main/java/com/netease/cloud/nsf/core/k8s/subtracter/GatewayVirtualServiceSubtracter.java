package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.proto.k8s.K8sTypes;
import com.netease.cloud.nsf.util.function.Subtracter;
import istio.networking.v1alpha3.VirtualServiceOuterClass;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/17
 **/
public class GatewayVirtualServiceSubtracter implements Subtracter<K8sTypes.VirtualService> {

    private String key;

    public GatewayVirtualServiceSubtracter(String key) {
        this.key = key;
    }

    @Override
    public K8sTypes.VirtualService subtract(K8sTypes.VirtualService old) {
        List<VirtualServiceOuterClass.HTTPRoute> latestHttp = old.getSpec().getHttpList().stream()
                .filter(h -> !h.getName().equals(key))
                .collect(Collectors.toList());

        VirtualServiceOuterClass.VirtualService build = old.getSpec().toBuilder().clearHttp().build();
        old.setSpec(build.toBuilder().addAllHttp(latestHttp).build());
        return old;
    }
}
