package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.GlobalPlugins;
import com.netease.cloud.nsf.service.PluginService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/13
 **/
public class GatewayPluginDataHandler implements DataHandler<GlobalPlugins> {

    private PluginService pluginService;

    public GatewayPluginDataHandler(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @Override
    public List<TemplateParams> handle(GlobalPlugins gp) {

//        pluginService do sth


        TemplateParams pmParams = TemplateParams.instance()
                .put(GATEWAY_PLUGIN_NAME, gp.getCode())
                .put(GATEWAY_PLUGIN_GATEWAYS, gp.getGateway() == null ? Collections.emptyList() : Arrays.asList(gp.getGateway()))
                .put(GATEWAY_PLUGIN_HOSTS, gp.getHosts())
//                .put(GATEWAY_PLUGIN_PLUGINS, gp.getPlugins())

                ;
        return Arrays.asList(pmParams);
    }
}
