package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.gateway.handler.DataHandler;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/10
 **/
public class RateLimiterDataHandler implements DataHandler<ServiceMeshRateLimit> {

    List<FragmentHolder> fragmentHolders;

    public RateLimiterDataHandler(List<FragmentHolder> fragmentHolder) {
        this.fragmentHolders = fragmentHolder;
    }

    @Override
    public List<TemplateParams> handle(ServiceMeshRateLimit rateLimit) {

        if (CollectionUtils.isEmpty(fragmentHolders)) return Collections.EMPTY_LIST;
        String smartLimiterConfig = fragmentHolders.get(0).getSharedConfigFragment().getContent();
        String gatewayPluginConfig = fragmentHolders.get(0).getGatewayPluginsFragment().getContent();

        TemplateParams tp = TemplateParams.instance()
                .put(SMART_LIMITER_NAME, rateLimit.getHost())
                .put(NAMESPACE, rateLimit.getNamespace())
                .put(SMART_LIMITER_CONFIG, smartLimiterConfig)
                .put(GATEWAY_HOSTS, Arrays.asList(rateLimit.getHost()))
                .put(GATEWAY_PLUGIN_NAME, rateLimit.getHost())
                .put(GATEWAY_PLUGIN_PLUGINS, Arrays.asList(gatewayPluginConfig));

        return Arrays.asList(tp);
    }

}
