package com.netease.cloud.nsf.core.k8s.operator;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.Endpoint;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntry;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntryBuilder;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntrySpec;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/19
 **/
@Component
public class ServiceEntryOperator implements k8sResourceOperator<ServiceEntry> {

    @Override
    public ServiceEntry merge(ServiceEntry old, ServiceEntry fresh) {
        ServiceEntry latest = new ServiceEntryBuilder(old).build();

        ServiceEntrySpec freshSpec = fresh.getSpec();
        ServiceEntrySpec latestSpec = latest.getSpec();
        // 直接覆盖ports
        latestSpec.setPorts(freshSpec.getPorts());
        // 合并新的和旧的endpoints
        latestSpec.setEndpoints(
                mergeList(latestSpec.getEndpoints(), freshSpec.getEndpoints(), new ServiceEntryEndpointEqual()));

        return latest;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.ServiceEntry.name().equals(name);
    }

    public static class ServiceEntryEndpointEqual implements Equals<Endpoint> {

        @Override
        public boolean apply(Endpoint oe, Endpoint ne) {
            return Objects.equals(oe.getLabels(), ne.getLabels());
        }
    }

    @Override
    public boolean isUseless(ServiceEntry serviceEntry) {
        return serviceEntry == null ||
                StringUtils.isEmpty(serviceEntry.getApiVersion()) ||
                 serviceEntry.getSpec() == null;
    }

    @Override
    public ServiceEntry subtract(ServiceEntry old, String value) {
        old.setSpec(null);
        return old;
    }
}
