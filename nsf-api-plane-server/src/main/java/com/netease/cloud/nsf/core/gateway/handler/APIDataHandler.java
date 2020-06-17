package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.gateway.handler.meta.UriMatchMeta;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.PairMatch;
import com.netease.cloud.nsf.meta.UriMatch;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.PriorityUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

public abstract class APIDataHandler implements DataHandler<API> {

    @Override
    public List<TemplateParams> handle(API api) {
        TemplateParams tp = handleApi(api);
        return doHandle(tp, api);
    }

    public TemplateParams handleApi(API api) {
        UriMatchMeta uriMatchMeta = getUris(api);
        String methods = getMethods(api);
        String apiName = getApiName(api);
        // host用于virtualservice的host
        List<String> hostList = productHostList(api);
        // host用于match中的header match
        String hostHeaders = produceHostHeaders(api);
        int priority = PriorityUtil.calculate(api);

        TemplateParams tp = TemplateParams.instance()
                .put(NAMESPACE, api.getNamespace())
                .put(API_SERVICE, api.getService())
                .put(API_NAME, api.getName())
                .put(API_IDENTITY_NAME, apiName)
                .put(API_LOADBALANCER, api.getLoadBalancer())
                .put(API_GATEWAYS, api.getGateways())
                .put(API_REQUEST_URIS, uriMatchMeta.getUri())
                .put(VIRTUAL_SERVICE_URL_MATCH, uriMatchMeta.getUriMatch())
                .put(API_MATCH_PLUGINS, api.getPlugins())
                .put(API_METHODS, methods)
                .put(API_RETRIES, api.getRetries())
                .put(API_PRESERVE_HOST, api.getPreserveHost())
                .put(API_HEADERS, api.getHeaders())
                .put(API_QUERY_PARAMS, api.getQueryParams())
                .put(API_CONNECT_TIMEOUT, api.getConnectTimeout())
                .put(API_IDLE_TIMEOUT, api.getIdleTimeout())
                .put(GATEWAY_HOSTS, api.getHosts())
                .put(VIRTUAL_SERVICE_MATCH_PRIORITY, priority)
                .put(VIRTUAL_SERVICE_HOSTS, hostList)
                .put(API_PRIORITY, api.getPriority())
                .put(VIRTUAL_SERVICE_SERVICE_TAG, api.getServiceTag())
                .put(VIRTUAL_SERVICE_API_ID, api.getApiId())
                .put(VIRTUAL_SERVICE_API_NAME, api.getApiName())
                .put(VIRTUAL_SERVICE_TENANT_ID, api.getTenantId())
                .put(VIRTUAL_SERVICE_PROJECT_ID, api.getProjectId())
                .put(VIRTUAL_SERVICE_HOST_HEADERS, hostHeaders)
                .put(VIRTUAL_SERVICE_TIME_OUT, api.getTimeout())
                .put(VIRTUAL_SERVICE_RETRY_ATTEMPTS, api.getAttempts())
                .put(VIRTUAL_SERVICE_RETRY_PER_TIMEOUT, api.getPerTryTimeout())
                .put(VIRTUAL_SERVICE_RETRY_RETRY_ON, api.getRetryOn())
                .put(SERVICE_INFO_API_GATEWAY, getGateways(api))
                .put(SERVICE_INFO_API_NAME, apiName)
                .put(SERVICE_INFO_API_SERVICE, getOrDefault(api.getService(), "NoneService"))
                .put(SERVICE_INFO_API_METHODS, getOrDefault(methods, ".*"))
                .put(SERVICE_INFO_API_REQUEST_URIS, getOrDefault(uriMatchMeta.getUri(), ".*"))
                .put(SERVICE_INFO_VIRTUAL_SERVICE_HOST_HEADERS, getOrDefault(hostHeaders, ".*"))
                .put(VIRTUAL_SERVICE_REQUEST_HEADERS, api.getRequestOperation())
                .put(VIRTUAL_SERVICE_VIRTUAL_CLUSTER_NAME, api.getVirtualClusterName())
                .put(VIRTUAL_SERVICE_VIRTUAL_CLUSTER_HEADERS, getVirtualClusterHeaders(api));

        return tp;
    }

    protected String getOrDefault(String value, String defaultValue) {
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    private String getMethods(API api) {

        List<String> methods = new ArrayList<>();
        for (String m : api.getMethods()) {
            if (m.equals("*")) return "";
            methods.add(m);
        }
        return String.join("|", methods);
    }

    private List<PairMatch> getVirtualClusterHeaders(API api){
        List<PairMatch> virtualClusterHeaders = new ArrayList<>();
        //前端配置VirtualCluster headers
        virtualClusterHeaders.addAll(api.getVirtualClusterHeaders());
        //headers
        virtualClusterHeaders.addAll(api.getHeaders());
        //构造method
        String methods = getMethods(api);
        if (!StringUtils.isEmpty(methods)) {
            virtualClusterHeaders.add(new PairMatch(":method", methods, "regex"));
        }
        //构造path
        if (CollectionUtils.isEmpty(api.getVirtualClusterHeaders())) {
            virtualClusterHeaders.add(new PairMatch(":path", getVirutalClusterUris(api), "regex"));
        }
        //authority
        String authority = produceHostHeaders(api);
        if (!StringUtils.isEmpty(authority)) {
            virtualClusterHeaders.add(new PairMatch(":authority", authority, "regex"));
        }
        return virtualClusterHeaders;
    }

    abstract List<TemplateParams> doHandle(TemplateParams tp, API api);

    String getVirutalClusterUris(API api){
        final StringBuffer suffix = new StringBuffer();
        if (api.getUriMatch().equals(UriMatch.prefix) || api.getUriMatch().equals(UriMatch.exact)) {
            suffix.append(".*");
        }
        String  uri = String.join("|", api.getRequestUris().stream()
                    .map(u -> u + suffix.toString())
                    .collect(Collectors.toList()));
        return StringEscapeUtils.escapeJava(uri);
    }

    UriMatchMeta getUris(API api) {
        //only one path，return
        String uri;
        UriMatch uriMatch;
        if (!CollectionUtils.isEmpty(api.getRequestUris()) && api.getRequestUris().size() == 1
                && UriMatch.exact.equals(api.getUriMatch())) {
            uri = api.getRequestUris().get(0);
            uriMatch = api.getUriMatch();
        } else {
            final StringBuffer suffix = new StringBuffer();
            if (api.getUriMatch().equals(UriMatch.prefix)) {
                suffix.append(".*");
            }
            uriMatch = UriMatch.regex;
            uri = String.join("|", api.getRequestUris().stream()
                    .map(u -> u + suffix.toString())
                    .collect(Collectors.toList()));
        }
        return new UriMatchMeta(uriMatch, StringEscapeUtils.escapeJava(uri));
    }

    String getHosts(API api) {
        if (api.getHosts().contains("*")) return "";
        return String.join("|", api.getHosts().stream()
                .map(h -> CommonUtil.host2Regex(h))
                .collect(Collectors.toList()));
    }

    String getGateways(API api) {
        if (CollectionUtils.isEmpty(api.getGateways())) return "";
        return String.join("|", api.getGateways());
    }

    String produceHostHeaders(API api) {
        return getHosts(api);
    }

    List<String> productHostList(API api) {
        return api.getHosts();
    }

    public String getApiName(API api) {
        return api.getName();
    }
}
