package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.util.function.Equals;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceSpec;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
@Component
public class VirtualServiceOperator implements IstioResourceOperator<VirtualService> {

    @Override
    public VirtualService merge(VirtualService old, VirtualService fresh) {

        VirtualServiceSpec oldSpec = old.getSpec();
        VirtualServiceSpec freshSpec = fresh.getSpec();

        oldSpec.setHttp(mergeList(oldSpec.getHttp(), freshSpec.getHttp(), new HttpRouteEquals()));
        oldSpec.setHosts(mergeList(oldSpec.getHosts(), freshSpec.getHosts(),  (ot, nt) -> Objects.equals(ot, nt)));
        return old;
    }

    private class HttpRouteEquals implements Equals<HTTPRoute> {
        @Override
        public boolean apply(HTTPRoute ot, HTTPRoute nt) {
            return Objects.equals(ot.getName(), nt.getName());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.VirtualService.name().equals(name);
    }

}
