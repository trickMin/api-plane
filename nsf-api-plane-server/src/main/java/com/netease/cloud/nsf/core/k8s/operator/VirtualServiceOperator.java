package com.netease.cloud.nsf.core.k8s.operator;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
@Component
public class VirtualServiceOperator implements k8sResourceOperator<VirtualService> {

    @Override
    public VirtualService merge(VirtualService old, VirtualService fresh) {

        VirtualServiceSpec oldSpec = old.getSpec();
        VirtualServiceSpec freshSpec = fresh.getSpec();

        VirtualService latest = new VirtualServiceBuilder(old).build();

        List<HTTPRoute> latestHttp = mergeList(oldSpec.getHttp(), freshSpec.getHttp(), new HttpRouteEquals());
        latest.getSpec().setHttp(latestHttp);

        Map<String, ApiPlugins> latestPlugins = mergeMap(oldSpec.getPlugins(), freshSpec.getPlugins(), (o, n) -> Objects.equals(o, n));
        latest.getSpec().setPlugins(latestPlugins);

        latest.getSpec().setRateLimits(fresh.getSpec().getRateLimits());
        latest.getSpec().setHosts(fresh.getSpec().getHosts());
        return latest;
    }

    private class HttpRouteEquals implements Equals<HTTPRoute> {
        @Override
        public boolean apply(HTTPRoute ot, HTTPRoute nt) {
            return Objects.equals(ot.getApi(), nt.getApi());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.VirtualService.name().equals(name);
    }

    @Override
    public boolean isUseless(VirtualService virtualService) {
        return virtualService == null ||
                StringUtils.isEmpty(virtualService.getApiVersion()) ||
                 virtualService.getSpec() == null ||
                  CollectionUtils.isEmpty(virtualService.getSpec().getHttp());
    }

    @Override
    public VirtualService subtract(VirtualService old, String value) {

        //根据api name删除httpRoute
        List<HTTPRoute> latestHttp = old.getSpec().getHttp().stream()
                .filter(h -> !h.getApi().equals(value))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(old.getSpec().getPlugins())) {
            old.getSpec().getPlugins().entrySet().removeIf(e -> e.getKey().equals(value));
        }

        old.getSpec().setHttp(latestHttp);
        return old;
    }
}