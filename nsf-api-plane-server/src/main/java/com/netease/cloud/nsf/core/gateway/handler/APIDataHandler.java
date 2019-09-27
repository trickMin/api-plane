package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.UriMatch;
import com.netease.cloud.nsf.util.CommonUtil;

import java.util.List;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

public abstract class APIDataHandler implements DataHandler<API> {

    @Override
    public List<TemplateParams> handle(API api) {

        String uris = getUris(api);
        String methods = String.join("|", api.getMethods());
        String hosts = getHosts(api);

        TemplateParams tp = TemplateParams.instance()
                .put(NAMESPACE, api.getNamespace())
                .put(API_SERVICE, api.getService())
                .put(API_NAME, api.getName())
                .put(API_LOADBALANCER, api.getLoadBalancer())
                .put(API_GATEWAYS, api.getGateways())
                .put(API_REQUEST_URIS, uris)
                .put(API_MATCH_PLUGINS, api.getPlugins())
                .put(API_METHODS, methods)
                .put(API_RETRIES, api.getRetries())
                .put(API_PRESERVE_HOST, api.getPreserveHost())
                .put(API_CONNECT_TIMEOUT, api.getConnectTimeout())
                .put(API_IDLE_TIMEOUT, api.getIdleTimeout())
                .put(GATEWAY_HOSTS, api.getHosts())
                .put(VIRTUAL_SERVICE_HOSTS, hosts);

        return doHandle(tp, api);
    }

    abstract List<TemplateParams> doHandle(TemplateParams tp, API api);

    String getUris(API api) {

        final StringBuffer suffix = new StringBuffer();
        if (api.getUriMatch().equals(UriMatch.PREFIX)) {
            suffix.append(".*");
        }
        return String.join("|", api.getRequestUris().stream()
                .map(u -> u + suffix.toString())
                .collect(Collectors.toList()));
    }


    private String getHosts(API api) {
        return String.join("|", api.getHosts().stream()
                .map(h -> CommonUtil.host2Regex(h))
                .collect(Collectors.toList()));
    }
}
