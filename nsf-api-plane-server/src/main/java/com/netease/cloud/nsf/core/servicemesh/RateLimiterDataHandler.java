package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.gateway.handler.DataHandler;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/10
 **/
public class RateLimiterDataHandler implements DataHandler<ServiceMeshRateLimit> {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterDataHandler.class);

    FragmentHolder fragmentHolder;

    public RateLimiterDataHandler(FragmentHolder fragmentHolder) {
        this.fragmentHolder = fragmentHolder;
    }

    @Override
    public List<TemplateParams> handle(ServiceMeshRateLimit rateLimit) {

        if (fragmentHolder == null ||
                fragmentHolder.getGatewayPluginsFragment() == null ||
                fragmentHolder.getSharedConfigFragment() == null) {
            logger.warn("fragmentHolder lacks fragments");
            throw new ApiPlaneException("fragmentHolder lacks fragments");
        }

        TemplateParams tp = TemplateParams.instance()
                .put(SMART_LIMITER_NAME, rateLimit.getHost())
                .put(NAMESPACE, getNamespace(rateLimit.getHost()))
                .put(SMART_LIMITER_CONFIG, fragmentHolder.getSharedConfigFragment().getContent())
                .put(GATEWAY_HOSTS, Arrays.asList(rateLimit.getHost()))
                .put(GATEWAY_PLUGIN_NAME, rateLimit.getHost())
                .put(GATEWAY_PLUGIN_PLUGINS, Arrays.asList(fragmentHolder.getGatewayPluginsFragment().getContent()));

        return Arrays.asList(tp);
    }

    private String getNamespace(String host) {
        if (!host.contains(".")) {
            throw new ApiPlaneException("illegal argument host " + host);
        }

        return host.split("\\.")[1];
    }
}
