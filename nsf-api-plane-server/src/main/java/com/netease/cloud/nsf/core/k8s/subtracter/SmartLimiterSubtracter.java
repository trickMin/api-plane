package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.util.function.Subtracter;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiter;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/10
 **/
public class SmartLimiterSubtracter implements Subtracter<SmartLimiter> {

    @Override
    public SmartLimiter subtract(SmartLimiter smartLimiter) {
        smartLimiter.setSpec(null);
        return smartLimiter;
    }
}