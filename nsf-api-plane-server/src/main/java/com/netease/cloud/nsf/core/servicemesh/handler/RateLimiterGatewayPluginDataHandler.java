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
public class RateLimiterGatewayPluginDataHandler implements DataHandler<ServiceMeshRateLimit> {

    FragmentWrapper fragmentWrapper;

    public RateLimiterGatewayPluginDataHandler(FragmentWrapper fragmentWrapper) {
        this.fragmentWrapper = fragmentWrapper;
    }

    @Override
    public List<TemplateParams> handle(ServiceMeshRateLimit rateLimit) {

        if (fragmentWrapper == null) return Collections.EMPTY_LIST;
        String gatewayPluginConfig = fragmentWrapper.getContent();

        TemplateParams tp = TemplateParams.instance()
                .put(NAMESPACE, rateLimit.getNamespace())
                .put(GATEWAY_PLUGIN_NAME, rateLimit.getHost())
                .put(GATEWAY_PLUGIN_SERVICES, Arrays.asList(rateLimit.getHost()))
                .put(GATEWAY_PLUGIN_PLUGINS, Arrays.asList(gatewayPluginConfig));

        return Arrays.asList(tp);
    }

}
