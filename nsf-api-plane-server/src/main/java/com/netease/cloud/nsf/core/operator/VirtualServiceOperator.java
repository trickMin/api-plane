package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceSpec;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
@Component
public class VirtualServiceOperator implements IstioResourceOperator<VirtualService> {

    @Override
    public VirtualService merge(VirtualService old, VirtualService fresh) {

        VirtualServiceSpec oldSpec = old.getSpec();
        VirtualServiceSpec freshSpec = fresh.getSpec();

        VirtualService latest = new VirtualServiceBuilder(old).build();

        List<HTTPRoute> latestHttp = mergeList(oldSpec.getHttp(), freshSpec.getHttp(), new HttpRouteEquals());
        latest.getSpec().setHttp(latestHttp);
        // 根据每个http中的hosts，得到最终的hosts
        List<String> latestHosts = latestHttp.stream()
                .map(http -> http.getHosts())
                .flatMap(hosts -> hosts.stream())
                .distinct()
                .collect(Collectors.toList());

        latest.getSpec().setHosts(latestHosts);
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
                virtualService.getSpec() == null ||
                  CollectionUtils.isEmpty(virtualService.getSpec().getHttp());
    }

    @Override
    public VirtualService subtract(VirtualService old, String service, String name) {

        //先根据api name删除httpRoute
        List<HTTPRoute> latestHttp = old.getSpec().getHttp().stream()
                .filter(h -> !h.getApi().equals(name))
                .collect(Collectors.toList());
        //根据最新的httpRoute,重新计算hosts
        List<String> latestHosts = latestHttp.stream()
                .map(http -> http.getHosts())
                .flatMap(hosts -> hosts.stream())
                .distinct()
                .collect(Collectors.toList());

        old.getSpec().setHttp(latestHttp);
        old.getSpec().setHosts(latestHosts);
        return old;
    }
}
