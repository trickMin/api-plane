package com.netease.cloud.nsf.core.istio.operator;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntry;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntryBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/19
 **/
@Component
public class ServiceEntryOperator implements IstioResourceOperator<ServiceEntry> {

    @Override
    public ServiceEntry merge(ServiceEntry old, ServiceEntry fresh) {
        ServiceEntry latest = new ServiceEntryBuilder(old).build();
        latest.setSpec(fresh.getSpec());

        return latest;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.ServiceEntry.name().equals(name);
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
