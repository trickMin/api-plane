package com.netease.cloud.nsf.core.k8s.operator;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiter;
import org.springframework.stereotype.Component;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/10
 **/
@Component
public class SmartLimiterOperator implements k8sResourceOperator<SmartLimiter> {

    @Override
    public SmartLimiter merge(SmartLimiter old, SmartLimiter fresh) {
        return fresh;
    }

    @Override
    public SmartLimiter subtract(SmartLimiter old, String value) {
        return old;
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.SmartLimiter.name().equals(name);
    }

    @Override
    public boolean isUseless(SmartLimiter smartLimiter) {
        return smartLimiter == null ||
                smartLimiter.getSpec() == null;
    }
}
