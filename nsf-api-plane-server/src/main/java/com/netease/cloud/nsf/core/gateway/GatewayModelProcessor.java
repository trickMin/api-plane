package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.Endpoint;
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

import java.util.*;

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

        TemplateParams baseParams = initTemplateParams(api, namespace, endpoints);

        List<String> rawGateways = buildGateways(envoys, baseParams);
        List<String> rawVirtualServices = buildVirtualServices(envoys, baseParams);
        List<String> rawDestinationRules = buildDestinationRules(destinations, baseParams);

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

//        if (!CollectionUtils.isEmpty(api.getPlugins())) {

//            Set<String> pluginDestinations = api.getPlugins().stream()
//                    .map(p -> Arrays.asList(p)) //TODO 提供插件，返回destinations
//                    .flatMap(List::stream)
//                    .distinct()
//                    .filter(s -> !StringUtils.isEmpty(s))
//                    .collect(Collectors.toSet());
//            destinations.addAll(pluginDestinations);
//        }

        // 每个网关实例都有一套Gateway+VirtualService
//        envoys.stream().forEach( gateway -> {
//            Map<String, Object> apiResourceParams = createApiResourceParams(gateway, api, namespace, endpoints, pluginPieces);
//            String rawGw = templateTranslator.translate(baseGateway, apiResourceParams);
//            rawGateways.add(rawGw);
//
//            //TODO 得到插件片段，并渲染
//            String tempVs = templateTranslator.translate(baseVirtualService, apiResourceParams);
//            //二次渲染
//            String rawVs = templateTranslator.translate("tempVs", tempVs, apiResourceParams);
//            rawVirtualServices.add(rawVs);
//        });
//
//
//        // 网关实例共用DestinationRule
//        destinations.stream().forEach(proxyUri -> {
//            Map<String, Object> destinationParams = createDestinationParams(proxyUri, api, namespace, envoys);
//            rawDestinationRules.add(templateTranslator.translate(baseDestinationRule, destinationParams));
//        });


    private List<String> buildDestinationRules(Set<String> destinations, TemplateParams baseParams) {

        List<String> destinationRules = new ArrayList<>();
        //TODO handle destination first
        destinations.stream().forEach(proxyUri -> {
            TemplateParams destinationParams = TemplateParams.instance()
                    .setParent(baseParams)
                    .put(DESTINATION_RULE_HOST, proxyUri)
                    .put(DESTINATION_RULE_NAME, proxyUri.contains(".") ? proxyUri.substring(0, proxyUri.indexOf(".")) : proxyUri);
            destinationRules.add(templateTranslator.translate(baseDestinationRule, destinationParams.output()));
        });
        return destinationRules;
    }

    private List<String> buildVirtualServices(List<String> envoys, TemplateParams baseParams) {

        List<String> virtualservices = new ArrayList<>();
        envoys.stream().forEach( gw -> {
            TemplateParams gatewayParams = TemplateParams.instance()
                    .setParent(baseParams)
                    .put(GATEWAY_NAME, String.format("%s-%s", baseParams.get(API_SERVICE), gw))
                    .put(VIRTUAL_SERVICE_NAME, String.format("%s-%s", baseParams.get(API_SERVICE), gw))
                    .put(VIRTUAL_SERVICE_SUBSET_NAME, String.format("%s-%s-%s", baseParams.get(API_SERVICE), baseParams.get(API_NAME), gw));

            Map<String, Object> mergedParams = gatewayParams.output();
            //先基础渲染
            String tempVs = templateTranslator.translate(baseVirtualService, mergedParams);
            //二次渲染插件中的内容
            String rawVs = templateTranslator.translate("tempVs", tempVs, mergedParams);
            virtualservices.add(rawVs);
        });
        return virtualservices;
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

    private TemplateParams initTemplateParams(API api, String namespace, List<Endpoint> endpoints) {

        String uris = String.join("|", api.getRequestUris());
        String methods = String.join("|", api.getMethods());

        List<String> plugins = api.getPlugins();
//        List<String> destinations = api.getProxyUris();

        if (!CollectionUtils.isEmpty(plugins)) {
            //TODO parse plugin and get extra destinations
            //TODO get plugin pieces
        }

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
                .put(VIRTUAL_SERVICE_HOSTS, api.getHosts())
                .put(VIRTUAL_SERVICE_DESTINATIONS, produceMultipleDestinations(api, endpoints));

        return tp;
    }

    private Map<String, Object> createDestinationParams(String proxyUri, API api, String namespace, List<String> gateways) {

        Map<String, Object> params = new HashMap<>();
        String destinationRuleName = proxyUri.contains(".") ? proxyUri.substring(0, proxyUri.indexOf(".")) : proxyUri;
        params.put(DESTINATION_RULE_NAME, destinationRuleName);
        params.put(DESTINATION_RULE_HOST, proxyUri);
        params.put(NAMESPACE, namespace);
        params.put("api", api);
        params.put("gateway_instances", gateways);

        return params;
    }

    private Map<String, Object> createApiResourceParams(String gateway, API api, String namespace, List<Endpoint> endpoints, List<String> plugins) {

        String resourceName = String.format("%s-%s", api.getService(), gateway);
        String subsetName = String.format("%s-%s-%s", api.getService(), api.getName(), gateway);
        String uris = String.join("|", api.getRequestUris());
        String methods = String.join("|", api.getMethods());

        Map<String, Object> params = new HashMap<>();

        params.put("api", api);
        params.put("resource_name", resourceName);
        params.put("namespace", namespace);
        params.put("gateway_instance", gateway);
        params.put("plugins", plugins);
        params.put("subset_name", subsetName);
        params.put("methods", methods);
        // TODO 判断request uri匹配类型
        params.put("uris", uris);
        params.put("template_api", api.getName());
        params.put("template_uri", uris);
        params.put("template_subset", subsetName);
        params.put("destinations", produceMultipleDestinations(api, endpoints));
        return params;
    }

    private List<Map<String, Object>> produceMultipleDestinations(API api, List<Endpoint> endpoints) {
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
        return destinations;
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
    public IstioResource subtract(IstioResource old, String api) {
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

}
