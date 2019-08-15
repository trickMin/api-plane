package com.netease.cloud.nsf.core.gateway;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import me.snowdrop.istio.api.IstioResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;


/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
@Component
public class GatewayModelProcessor {

    private static final Logger logger = LoggerFactory.getLogger(GatewayModelProcessor.class);

    @Autowired
    IntegratedResourceOperator operator;

    @Autowired
    TemplateTranslator templateTranslator;

    @Autowired
    EditorContext editorContext;

    @Autowired
    IstioHttpClient istioHttpClient;

    @Autowired
    PluginService pluginService;

    private static final String baseGateway = "gateway/baseGateway";
    private static final String baseVirtualService = "gateway/baseVirtualService";
    private static final String baseDestinationRule = "gateway/baseDestinationRule";
    private static final String baseVirtualServiceMatch = "gateway/baseVirtualServiceMatch";
    private static final String baseVirtualServiceRoute = "gateway/baseVirtualServiceRoute";
    private static final String baseVirtualServiceExtra = "gateway/baseVirtualServiceExtra";

    /**
     * 将api转换为istio对应的规则
     *
     * @param api
     * @param namespace
     * @return
     */
    public List<IstioResource> translate(API api, String namespace) {

        List<IstioResource> resources = new ArrayList<>();
        List<String> envoys = api.getGateways();
        Set<String> destinations = new HashSet<>(api.getProxyUris());

        if (CollectionUtils.isEmpty(envoys)) throw new ApiPlaneException(ExceptionConst.GATEWAY_LIST_EMPTY);

        if (CollectionUtils.isEmpty(destinations)) throw new ApiPlaneException(ExceptionConst.PROXY_URI_LIST_EMPTY);

        List<Endpoint> endpoints = istioHttpClient.getEndpointList();
        if (CollectionUtils.isEmpty(endpoints)) throw new ApiPlaneException(ExceptionConst.ENDPOINT_LIST_EMPTY);

        TemplateParams baseParams = initTemplateParams(api, namespace);

        List<String> rawGateways = buildGateways(envoys, baseParams);
        List<String> rawVirtualServices = buildVirtualServices(api, baseParams, endpoints);
        List<String> rawDestinationRules = buildDestinationRules(api, baseParams);

        List<String> rawResources = new ArrayList<>();
        rawResources.addAll(rawGateways);
        rawResources.addAll(rawVirtualServices);
        rawResources.addAll(rawDestinationRules);

        rawResources.forEach(r -> logger.info(r));
        rawResources.stream()
                .forEach(r -> {
                    K8sResourceGenerator gen = K8sResourceGenerator.newInstance(r, ResourceType.YAML, editorContext);
                    K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
                    IstioResource ir = (IstioResource) gen.object(resourceEnum.mappingType());
                    resources.add(ir);
                });

        return resources;
    }

    private List<String> buildDestinationRules(API api, TemplateParams baseParams) {

        List<String> destinationRules = new ArrayList<>();
        Set<String> destinations = new HashSet(api.getProxyUris());
        // 根据插件中的目标服务 得到额外的destination rule
        if (!CollectionUtils.isEmpty(api.getPlugins())) {
            List<String> extraDestinations = pluginService.extractService(api.getPlugins());
            destinations.addAll(extraDestinations);
        }
        destinations.stream().forEach(proxyUri -> {
            TemplateParams destinationParams = TemplateParams.instance()
                    .setParent(baseParams)
                    .put(DESTINATION_RULE_HOST, proxyUri)
                    .put(DESTINATION_RULE_NAME, proxyUri.contains(".") ? proxyUri.substring(0, proxyUri.indexOf(".")) : proxyUri);
            destinationRules.add(templateTranslator.translate(baseDestinationRule, destinationParams.output()));
        });
        return destinationRules;
    }

    private List<String> buildVirtualServices(API api, TemplateParams baseParams, List<Endpoint> endpoints) {

        List<String> virtualservices = new ArrayList<>();
        api.getGateways().stream().forEach( gw -> {

            String subset = String.format("%s-%s-%s", baseParams.get(API_SERVICE), baseParams.get(API_NAME), gw);

            String route = produceRoute(api, endpoints, subset);
            String match = produceMatch(baseParams);
            String extra = productExtra(baseParams);

            TemplateParams gatewayParams = TemplateParams.instance()
                    .setParent(baseParams)
                    .put(GATEWAY_NAME, String.format("%s-%s", api.getService(), gw))
                    .put(VIRTUAL_SERVICE_NAME, String.format("%s-%s", api.getService(), gw))
                    .put(VIRTUAL_SERVICE_SUBSET_NAME, subset)
                    .put(VIRTUAL_SERVICE_MATCH, match)
                    .put(VIRTUAL_SERVICE_ROUTE, route)
                    .put(VIRTUAL_SERVICE_EXTRA, extra)
                    .put(API_PLUGINS, handlePlugins(api, match, route, extra));

            Map<String, Object> mergedParams = gatewayParams.output();
            //先基础渲染
            String tempVs = templateTranslator.translate(baseVirtualService, mergedParams);
            //二次渲染插件中的内容
            String rawVs = templateTranslator.translate("tempVs", tempVs, mergedParams);
            virtualservices.add(rawVs);
        });
        return virtualservices;
    }




    private List<String> handlePlugins(API api, String match, String route, String extra) {

        List<String> plugins = api.getPlugins();
        if (CollectionUtils.isEmpty(plugins)) return plugins;
        ServiceInfo service = new ServiceInfo();
        service.setApiName(wrap(API_NAME));
        service.setUri(wrap(API_REQUEST_URIS));
        service.setMethod(wrap(API_METHODS));
        service.setSubset(wrap(VIRTUAL_SERVICE_SUBSET_NAME));
        service.setApi(api);
        // TODO 给service传入 match, route, extra
        List<String> handledPlugins = plugins.stream()
                .map(p -> pluginService.processSchema(p, service).getVirtualServiceFragment())
                .collect(Collectors.toList());
        return handledPlugins;
    }

    private List<String> buildGateways(List<String> envoys, TemplateParams baseParams) {

        List<String> gateways = new ArrayList<>();
        envoys.stream().forEach( gw -> {

            TemplateParams gatewayParams = TemplateParams.instance()
                    .setParent(baseParams)
                    .put(API_GATEWAY, gw)
                    .put(GATEWAY_NAME, String.format("%s-%s", baseParams.get(API_SERVICE), gw));

            gateways.add(templateTranslator.translate(baseGateway, gatewayParams.output()));
        });
        return gateways;
    }

    /**
     * 初始化渲染所需的基本参数
     * @param api
     * @param namespace
     * @return
     */
    private TemplateParams initTemplateParams(API api, String namespace) {

        String uris = String.join("|", api.getRequestUris());
        String methods = String.join("|", api.getMethods());

        TemplateParams tp = TemplateParams.instance()
                .put(NAMESPACE, namespace)
                .put(API_SERVICE, api.getService())
                .put(API_NAME, api.getName())
                .put(API_LOADBALANCER, api.getLoadBalancer())
                .put(API_GATEWAYS, api.getGateways())
                .put(API_REQUEST_URIS, uris)
                .put(API_PLUGINS, api.getPlugins()) //TODO handle plugins
                .put(API_METHODS, methods)
                .put(GATEWAY_HOSTS, api.getHosts())
                .put(VIRTUAL_SERVICE_HOSTS, api.getHosts());

        return tp;
    }

    private String produceRoute(API api, List<Endpoint> endpoints, String subset) {
        List<Map<String, Object>> destinations = new ArrayList<>();
        List<String> proxies = api.getProxyUris();
        for (int i = 0; i < proxies.size() ; i++) {
            for (Endpoint e : endpoints) {
                if (e.getHostname().equals(proxies.get(i))) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("port", e.getPort());
                    int weight = 100/proxies.size();
                    if (i == proxies.size() - 1) {
                        weight = 100 - 100*(proxies.size()-1)/proxies.size();
                    }
                    param.put("weight", weight);
                    param.put("host", e.getHostname());
                    destinations.add(param);
                    break;
                }
            }
        }
        String destinationStr = templateTranslator
                .translate(baseVirtualServiceRoute,
                        ImmutableMap.of(VIRTUAL_SERVICE_DESTINATIONS, destinations,
                                        VIRTUAL_SERVICE_SUBSET_NAME, subset));
        return destinationStr;
    }

    private String productExtra(TemplateParams params) {
        return templateTranslator.translate(baseVirtualServiceExtra, params.output());
    }

    private String produceMatch(TemplateParams params) {
        return templateTranslator.translate(baseVirtualServiceMatch, params.output());
    }

    /**
     * 合并两个crd,新的和旧的重叠部分会用新的覆盖旧的
     * @param old
     * @param fresh
     * @return
     */
    public IstioResource merge(IstioResource old, IstioResource fresh) {

        if (fresh == null) return old;
        if (old == null) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        return operator.merge(old, fresh);
    }

    /**
     * 在已有的istio crd中删去对应api部分
     *
     * @param old
     * @param api
     * @return
     */
    public IstioResource subtract(IstioResource old, String service, String api) {
        K8sResourceEnum resource = K8sResourceEnum.get(old.getKind());
        switch (resource) {
            case VirtualService: {
                ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT, editorContext);
                gen.removeElement(PathExpressionEnum.REMOVE_VS_HTTP.translate(api));
                return (IstioResource) gen.object(resource.mappingType());
            }
            case DestinationRule: {
                ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT, editorContext);
                gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET.translate(api));
                return (IstioResource) gen.object(resource.mappingType());
            }
            default:
                return old;
        }
    }

    public String wrap(String raw) {
        if (StringUtils.isEmpty(raw)) throw new NullPointerException();
        return "${" + raw + "}";
    }

}
