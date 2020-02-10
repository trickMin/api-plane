package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.GlobalPlugins;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/13
 **/
public class GatewayPluginDataHandler implements DataHandler<GlobalPlugins> {

    List<FragmentWrapper> fragments;
    List<Gateway> gateways;

    public GatewayPluginDataHandler(List<FragmentWrapper> fragments, List<Gateway> gateways) {
        this.fragments = fragments;
        this.gateways = gateways;
    }

    @Override
    public List<TemplateParams> handle(GlobalPlugins gp) {

        List<String> plugins = extractFragments(fragments);
        TemplateParams pmParams = TemplateParams.instance()
                .put(GATEWAY_PLUGIN_NAME, gp.getCode())
                .put(GATEWAY_PLUGIN_GATEWAYS, getGateways(gp))
                .put(GATEWAY_PLUGIN_HOSTS, gp.getHosts())
                .put(GATEWAY_PLUGIN_PLUGINS, plugins);
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

    List<String> getGateways(GlobalPlugins gp) {
        if (gp.getGateway() == null) return Collections.emptyList();
        if (Pattern.compile("(.*?)/(.*?)").matcher(gp.getGateway()).find()) return Arrays.asList(gp.getGateway());

        return Arrays.asList(String.format("%s/%s", getNamespace(gp.getGateway()), gp.getGateway()));
    }

    private String getNamespace(String gateway) {
        final String gwClusgterKey = "gw_cluster";
        for (Gateway item : gateways) {
            if (Objects.nonNull(item.getLabels()) && Objects.equals(gateway, item.getLabels().get(gwClusgterKey))) {
                Pattern pattern = Pattern.compile("(.*?)\\.(.*?)\\.svc\\.cluster\\.(.*?)");
                Matcher matcher = pattern.matcher(item.getHostname());
                if (matcher.find()) {
                    return matcher.group(2);
                }
                throw new ApiPlaneException(String.format("The gateway [%s]`s hostname [%s] is not compliant", gateway, item.getHostname()));
            }
        }
        throw new ApiPlaneException(String.format("The gateway [%s] endpoint could not be found.", gateway));
    }
}
