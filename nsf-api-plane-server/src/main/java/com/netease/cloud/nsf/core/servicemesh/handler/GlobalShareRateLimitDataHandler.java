package com.netease.cloud.nsf.core.servicemesh.handler;

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
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/5/12
 **/
public class GlobalShareRateLimitDataHandler implements DataHandler<ServiceMeshRateLimit> {

    List<FragmentHolder> fragmentHolders;
    String configMapName;

    public GlobalShareRateLimitDataHandler(List<FragmentHolder> fragmentHolder, String configMapName) {
        this.fragmentHolders = fragmentHolder;
        this.configMapName = configMapName;
    }

    @Override
    public List<TemplateParams> handle(ServiceMeshRateLimit rateLimit) {

        if (CollectionUtils.isEmpty(fragmentHolders)) return Collections.EMPTY_LIST;
        //FIXME 插件未适配 临时适配
        String configMapConfig = fragmentHolders.get(0).getSharedConfigFragment().getContent().replaceFirst("-", " ");
        String gatewayPluginConfig = fragmentHolders.get(0).getGatewayPluginsFragment().getContent();

        TemplateParams tp = TemplateParams.instance()
                .put(RLS_CONFIG_MAP_NAME, configMapName)
                .put(NAMESPACE, rateLimit.getNamespace())
                .put(RLS_CONFIG_MAP_DESCRIPTOR, Arrays.asList(configMapConfig))
                .put(GATEWAY_PLUGIN_NAME, rateLimit.getHost())
                .put(GATEWAY_PLUGIN_SERVICES, Arrays.asList(rateLimit.getHost()))
                .put(GATEWAY_PLUGIN_PLUGINS, Arrays.asList(gatewayPluginConfig));

        return Arrays.asList(tp);
    }



}
