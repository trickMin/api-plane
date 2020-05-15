package com.netease.cloud.nsf.core.servicemesh.handler;

import com.netease.cloud.nsf.core.gateway.handler.DataHandler;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/10
 **/
public class RateLimiterSmartLimiterDataHandler implements DataHandler<ServiceMeshRateLimit> {

    FragmentWrapper fragmentWrapper;

    public RateLimiterSmartLimiterDataHandler(FragmentWrapper fragmentWrapper) {
        this.fragmentWrapper = fragmentWrapper;
    }

    @Override
    public List<TemplateParams> handle(ServiceMeshRateLimit rateLimit) {

        if (fragmentWrapper == null) return Collections.EMPTY_LIST;
        String smartLimiterConfig = fragmentWrapper.getContent();

        TemplateParams tp = TemplateParams.instance()
                .put(SMART_LIMITER_NAME, rateLimit.getServiceName())
                .put(NAMESPACE, rateLimit.getNamespace())
                .put(SMART_LIMITER_CONFIG, smartLimiterConfig);

        return Arrays.asList(tp);
    }

}
