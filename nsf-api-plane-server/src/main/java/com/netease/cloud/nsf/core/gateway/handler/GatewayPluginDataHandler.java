package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.GlobalPlugin;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/13
 **/
public class GatewayPluginDataHandler implements DataHandler<GlobalPlugin> {

    List<FragmentWrapper> fragments;

    public GatewayPluginDataHandler(List<FragmentWrapper> fragments) {
        this.fragments = fragments;
    }

    @Override
    public List<TemplateParams> handle(GlobalPlugin gp) {

        List<String> plugins = extractFragments(fragments);
        TemplateParams pmParams = TemplateParams.instance()
                .put(GATEWAY_PLUGIN_NAME, gp.getCode())
                .put(GATEWAY_PLUGIN_GATEWAYS, gp.getGateway() == null ? Collections.emptyList() : Arrays.asList(gp.getGateway()))
                .put(GATEWAY_PLUGIN_HOSTS, gp.getHosts())
                .put(GATEWAY_PLUGIN_PLUGINS, plugins)

                ;
        return doHandle(pmParams);
    }

    List<TemplateParams> doHandle(TemplateParams params) {
        return Arrays.asList(params);
    }

    List<String> extractFragments(List<FragmentWrapper> fragments) {
        List<String> plugins = Collections.emptyList();
        if (!CollectionUtils.isEmpty(fragments)) {
            plugins = fragments.stream()
                    .filter(f -> f != null)
                    .map(f -> f.getContent())
                    .collect(Collectors.toList());
        }
        return plugins;
    }
}
