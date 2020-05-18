package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.util.HandlerUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;


/**
 * api级别的全局插件
 *
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/3/18
 **/
public class ApiGatewayPluginDataHandler extends APIDataHandler {

    String gatewayNamespace;
    List<FragmentWrapper> fragments;

    public ApiGatewayPluginDataHandler(List<FragmentWrapper> fragments, String gatewayNamespace) {
        this.fragments = fragments;
        this.gatewayNamespace = gatewayNamespace;
    }

    @Override
    List<TemplateParams> doHandle(TemplateParams tp, API api) {

        if (api == null) return Collections.EMPTY_LIST;
        Map<String, List<String>> apiPlugins = HandlerUtil.getApiPlugins(fragments);
        List<String> gateways = api.getGateways();
        List<TemplateParams> params = new ArrayList<>();
        List<String> routes = api.getHosts().stream()
                .map(h -> h + "/" + api.getName())
                .collect(Collectors.toList());

        gateways.forEach(gw -> {
            TemplateParams pmParams = TemplateParams.instance()
                    .setParent(tp)
                    .put(GATEWAY_PLUGIN_NAME, getGatewayPluginName(gw, api.getName()))
                    .put(RESOURCE_IDENTITY, getIdentity(api.getName(), gw))
                    .put(GATEWAY_PLUGIN_GATEWAYS, Arrays.asList(String.format("%s/%s", gatewayNamespace, gw)))
                    .put(GATEWAY_PLUGIN_ROUTES, routes)
                    .put(GATEWAY_PLUGIN_PLUGINS, apiPlugins);

            params.addAll(doHandle(pmParams));
        });

        return params;
    }

    public String getGatewayPluginName(String gateway, String api) {

        List<String> parts = new ArrayList<>();

        if (!StringUtils.isEmpty(api)) {
            parts.add(api);
        }
        if (!StringUtils.isEmpty(gateway)) {
            parts.add(gateway);
        }

        if (CollectionUtils.isEmpty(parts)) return null;
        return String.join("-", parts);
    }

    public String getIdentity(String api, String gw) {
        return String.format("%s-%s", api, gw);
    }

    List<TemplateParams> doHandle(TemplateParams params) {
        return Arrays.asList(params);
    }

}
