package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceSpec;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
        oldSpec.setHosts(mergeList(oldSpec.getHosts(), freshSpec.getHosts(), (ot, nt) -> Objects.equals(ot, nt)));

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

    @Override
    public boolean isUseless(VirtualService virtualService) {
        return virtualService == null ||
                virtualService.getSpec() == null ||
                  CollectionUtils.isEmpty(virtualService.getSpec().getHttp());
    }

    @Override
    public VirtualService subtract(VirtualService old, String service, String name) {
        ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT);
        gen.removeElement(PathExpressionEnum.REMOVE_VS_HTTP.translate(name));
        return gen.object(VirtualService.class);
    }
}
