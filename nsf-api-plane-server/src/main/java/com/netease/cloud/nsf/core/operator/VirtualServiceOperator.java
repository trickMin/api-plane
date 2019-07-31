package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.meta.K8sResourceEnum;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
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

        // 删除原先资源中相同的http和hosts，然后再合并
        List<HTTPRoute> filteredHttp = filterSameHttpRoute(oldSpec, freshSpec);
        List<String> filteredHosts = filterSameHosts(oldSpec, freshSpec);

        oldSpec.setHttp(mergeList(filteredHttp, freshSpec.getHttp()));
        oldSpec.setHosts(mergeList(filteredHosts, freshSpec.getHosts()));
        return old;
    }

    private List<HTTPRoute> filterSameHttpRoute(VirtualServiceSpec oldSpec, VirtualServiceSpec freshSpec) {

        if (CollectionUtils.isEmpty(oldSpec.getHttp())) return freshSpec.getHttp();
        if (CollectionUtils.isEmpty(freshSpec.getHttp())) return oldSpec.getHttp();

        return oldSpec.getHttp().stream()
                .filter(oh -> {
                    for (HTTPRoute fh : freshSpec.getHttp()) {
                        if (Objects.equals(fh.getName(), oh.getName())) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    private List<String> filterSameHosts(VirtualServiceSpec oldSpec, VirtualServiceSpec freshSpec) {

        if (CollectionUtils.isEmpty(oldSpec.getHosts())) return freshSpec.getHosts();
        if (CollectionUtils.isEmpty(freshSpec.getHosts())) return oldSpec.getHosts();

        return oldSpec.getHosts().stream()
                .filter(oh -> {
                    for (String fh : freshSpec.getHosts()) {
                        if (Objects.equals(fh, oh)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }


    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.VirtualService.name().equals(name);
    }

}
