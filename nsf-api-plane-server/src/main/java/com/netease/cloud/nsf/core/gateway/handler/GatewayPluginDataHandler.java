package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateConst;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.GatewayPlugin;
import com.netease.cloud.nsf.util.HandlerUtil;
import com.netease.cloud.nsf.util.constant.PluginConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 路由插件CRD处理器
 *
 * @author yutao04
 * @date 2021.12.07
 **/
public class GatewayPluginDataHandler implements DataHandler<GatewayPlugin> {

    String gatewayNamespace;
    List<FragmentWrapper> fragments;

    public GatewayPluginDataHandler(List<FragmentWrapper> fragments, String gatewayNamespace) {
        this.fragments = fragments;
        this.gatewayNamespace = gatewayNamespace;
    }

    @Override
    public List<TemplateParams> handle(GatewayPlugin plugin) {
        if (plugin == null) {
            return Collections.EMPTY_LIST;
        }
        List<TemplateParams> params = new ArrayList<>();
        Map<String, List<String>> gatewayPluginMap = HandlerUtil.getGatewayPlugins(fragments);
        TemplateParams gatewayPluginParams = TemplateParams.instance()
                .put(TemplateConst.GATEWAY_PLUGIN_GATEWAYS, getGatewayName(plugin))
                .put(TemplateConst.GATEWAY_PLUGIN_NAME, getGatewayPluginName(plugin))
                .put(TemplateConst.GATEWAY_PLUGIN_PLUGINS, gatewayPluginMap);

        // 路由和全局插件模板渲染数据区分填充
        if (plugin.isRoutePlugin()) {
            gatewayPluginParams
                    .put(TemplateConst.GATEWAY_PLUGIN_ROUTES, getRouteList(plugin))
                    .put(TemplateConst.RESOURCE_IDENTITY, getIdentity(plugin))
                    .put(TemplateConst.SERVICE_INFO_API_SERVICE, PluginConstant.DEFAULT_SERVICE_NAME)
                    .put(TemplateConst.SERVICE_INFO_API_GATEWAY, plugin.getGateway())
                    .put(TemplateConst.SERVICE_INFO_API_NAME, plugin.getRouteId());
        } else if (plugin.isGlobalPlugin()) {
            gatewayPluginParams.put(TemplateConst.GATEWAY_PLUGIN_HOSTS, plugin.getHosts());
        }

        params.addAll(Arrays.asList((gatewayPluginParams)));

        return params;
    }

    private String getIdentity(GatewayPlugin plugin) {
        return String.format("%s-%s", plugin.getRouteId(), plugin.getGateway());
    }

    private List<String> getRouteList(GatewayPlugin plugin) {
        final String routeId = plugin.getRouteId();
        return plugin.getHosts().stream()
                .map(host -> host + "/" + routeId)
                .collect(Collectors.toList());
    }

    private List<String> getGatewayName(GatewayPlugin plugin) {
        return Collections.singletonList(String.format("%s/%s", gatewayNamespace, plugin.getGateway()));
    }

    private String getGatewayPluginName(GatewayPlugin plugin) {
        String pluginName = PluginConstant.DEFAULT_PLUGIN_NAME;

        if (plugin.isRoutePlugin()) {
            pluginName = plugin.getRouteId() + "-" + plugin.getGateway();
        } else if (plugin.isGlobalPlugin()) {
            pluginName = plugin.getCode().toLowerCase();
        }
        return pluginName;
    }
}
