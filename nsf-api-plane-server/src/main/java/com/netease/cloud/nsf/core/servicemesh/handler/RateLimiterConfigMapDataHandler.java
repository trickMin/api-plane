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
public class RateLimiterConfigMapDataHandler implements DataHandler<ServiceMeshRateLimit> {

    FragmentWrapper fragmentWrapper;
    String configMapName;

    public RateLimiterConfigMapDataHandler(FragmentWrapper fragmentWrapper, String configMapName) {
        this.fragmentWrapper = fragmentWrapper;
        this.configMapName = configMapName;
    }

    @Override
    public List<TemplateParams> handle(ServiceMeshRateLimit rateLimit) {

        if (fragmentWrapper == null) return Collections.EMPTY_LIST;
        String configMapConfig = fragmentWrapper.getContent();

        TemplateParams tp = TemplateParams.instance()
                .put(RLS_CONFIG_MAP_NAME, configMapName)
                .put(RLS_CONFIG_MAP_DESCRIPTOR, Arrays.asList(configMapConfig));

        return Arrays.asList(tp);
    }

}
