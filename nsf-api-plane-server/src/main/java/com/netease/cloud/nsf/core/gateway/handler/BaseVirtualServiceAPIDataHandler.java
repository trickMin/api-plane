package com.netease.cloud.nsf.core.gateway.handler;


import com.netease.cloud.nsf.core.gateway.processor.ModelProcessor;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.PriorityUtil;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class BaseVirtualServiceAPIDataHandler extends APIDataHandler {

    static final String apiVirtualServiceMatch = "gateway/api/virtualServiceMatch";
    static final String apiVirtualServiceExtra = "gateway/api/virtualServiceExtra";
    static final String apiVirtualServiceApi = "gateway/api/virtualServiceApi";
    static final String apiVirtualServiceRoute = "gateway/api/virtualServiceRoute";
    static final String apiVirtualServiceHttpRetry = "gateway/api/virtualServiceHttpRetry";

    static final String defaultUserId = "";

    ModelProcessor subModelProcessor;
    List<FragmentWrapper> fragments;
    List<Endpoint> endpoints;
    boolean simple;

    public BaseVirtualServiceAPIDataHandler(ModelProcessor subModelProcessor, List<FragmentWrapper> fragments, List<Endpoint> endpoints, boolean simple) {
        this.subModelProcessor = subModelProcessor;
        this.fragments = fragments;
        this.endpoints = endpoints;
        this.simple = simple;
    }

    @Override
    List<TemplateParams> doHandle(TemplateParams baseParams, API api) {

        // 插件分为match、api、host三个级别
        List<String> matchPlugins = new ArrayList<>();
        // api下的插件 可以根据插件划分
        Map<String, List<String>> apiPlugins = new HashMap<>();
        List<String> hostPlugins = new ArrayList<>();

        distributePlugins(fragments, matchPlugins, apiPlugins, hostPlugins);

        String httpApiYaml = produceHttpApi(baseParams);
        int pluginPriority = calculatePluginPriority(api, baseParams.get(VIRTUAL_SERVICE_MATCH_PRIORITY));

        String matchYaml = produceMatch(baseParams);
        String httpRetryYaml = produceHttpRetry(baseParams);
        TemplateParams vsParams = TemplateParams.instance()
                .setParent(baseParams)
                .put(VIRTUAL_SERVICE_MATCH_YAML, matchYaml)
                .put(VIRTUAL_SERVICE_API_YAML, httpApiYaml)
                .put(API_MATCH_PLUGINS, matchPlugins)
                .put(API_API_PLUGINS, apiPlugins)
                .put(API_HOST_PLUGINS, hostPlugins)
                .put(VIRTUAL_SERVICE_HTTP_RETRY_YAML, httpRetryYaml)
                .put(VIRTUAL_SERVICE_PLUGIN_MATCH_PRIORITY, pluginPriority)
                .put(SERVICE_INFO_VIRTUAL_SERVICE_PLUGIN_MATCH_PRIORITY, pluginPriority);

        List<TemplateParams> collect = api.getGateways().stream()
                .map(gw -> {
                    String subset = buildVirtualServiceSubsetName(api.getService(), api.getName(), gw);
                    String route = produceRoute(api, endpoints, subset);

                    return TemplateParams.instance()
                            .setParent(vsParams)
                            .put(GATEWAY_NAME, buildGatewayName(api.getService(), gw))
                            .put(VIRTUAL_SERVICE_NAME, buildVirtualServiceName(api.getService(), api.getName(), gw))
                            .put(VIRTUAL_SERVICE_SUBSET_NAME, subset)
                            .put(VIRTUAL_SERVICE_ROUTE_YAML, route)
                            .put(VIRTUAL_SERVICE_EXTRA_YAML, productExtra(vsParams))
                            .put(SERVICE_INFO_VIRTUAL_SERVICE_SUBSET_NAME, subset);
                })
                .collect(Collectors.toList());

        return collect;
    }

    String buildVirtualServiceName(String serviceName, String apiName, String gw) {
        return String.format("%s-%s-%s", serviceName, apiName, gw);
    }

    String productExtra(TemplateParams params) {
        return subModelProcessor.process(apiVirtualServiceExtra, params);
    }

    String produceMatch(TemplateParams params) {
        return subModelProcessor.process(apiVirtualServiceMatch, params);
    }

    String produceHttpRetry(TemplateParams params) {
        return subModelProcessor.process(apiVirtualServiceHttpRetry, params);
    }

    String produceHttpApi(TemplateParams params) {
        return subModelProcessor.process(apiVirtualServiceApi, params);
    }

    String buildVirtualServiceSubsetName(String serviceName, String apiName, String gw) {
        return String.format("%s-%s-%s", serviceName, apiName, gw);
    }

    String buildGatewayName(String service, String gw) {
        return gw;
    }

    String produceRoute(API api, List<Endpoint> endpoints, String subset) {

        if (simple) return "";
        List<Map<String, Object>> destinations = new ArrayList<>();
        List<String> proxies = api.getProxyUris();

        for (int i = 0; i < proxies.size(); i++) {
            boolean isMatch = false;
            for (Endpoint e : endpoints) {
                if (e.getHostname().equals(proxies.get(i))) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("port", e.getPort());
                    int weight = 100 / proxies.size();
                    if (i == proxies.size() - 1) {
                        weight = 100 - 100 * (proxies.size() - 1) / proxies.size();
                    }
                    param.put("weight", weight);
                    param.put("host", e.getHostname());
                    param.put("subset", subset);
                    destinations.add(param);
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch)
                throw new ApiPlaneException(String.format("%s:%s", ExceptionConst.TARGET_SERVICE_NON_EXIST, proxies.get(i)));
        }

        String destinationStr = subModelProcessor.process(apiVirtualServiceRoute, TemplateParams.instance().put(VIRTUAL_SERVICE_DESTINATIONS, destinations));
        return destinationStr;
    }

    String decorateHost(String code) {
        return String.format("com.netease.%s", code);
    }

    /**
     * 分配插件
     *
     * @param fragments
     * @param matchPlugins
     * @param apiPlugins
     * @param hostPlugins
     */
    void distributePlugins(List<FragmentWrapper> fragments, List<String> matchPlugins, Map<String, List<String>> apiPlugins, List<String> hostPlugins) {
        fragments.stream()
                .forEach(f -> {
                    switch (f.getFragmentType()) {
                        case VS_MATCH:
                            matchPlugins.add(f.getContent());
                            break;
                        case VS_API:
                            String userId = StringUtils.isEmpty(f.getXUserId()) ? defaultUserId : f.getXUserId();
                            apiPlugins.computeIfAbsent(userId, k -> new ArrayList<>()).add(f.getContent());
                            break;
                        case VS_HOST:
                            hostPlugins.add(f.getContent());
                            break;
                        default:
                    }
                });
    }

    int calculatePluginPriority(API api, Object parentPriority) {
        if (Objects.nonNull(parentPriority) && parentPriority instanceof Integer) {
            return (Integer) parentPriority + 1;
        } else {
            return PriorityUtil.calculate(api);
        }
    }
}
