package com.netease.cloud.nsf.core.gateway;

import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.Criteria;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.core.istio.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.web.PortalService;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.K8sResourceEnum;
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
    ResourceManager resourceManager;

    @Autowired
    PluginService pluginService;

    private static final String apiGateway = "gateway/api/gateway";
    private static final String apiVirtualService = "gateway/api/virtualService";
    private static final String apiDestinationRule = "gateway/api/destinationRule";
    private static final String apiVirtualServiceMatch = "gateway/api/virtualServiceMatch";
    private static final String apiVirtualServiceRoute = "gateway/api/virtualServiceRoute";
    private static final String apiVirtualServiceExtra = "gateway/api/virtualServiceExtra";
    private static final String apiVirtualServiceApi = "gateway/api/virtualServiceApi";
    private static final String apiSharedConfig = "gateway/api/sharedConfig";


    private static final String serviceDestinationRule = "gateway/service/destinationRule";
    private static final String serviceServiceEntry = "gateway/service/serviceEntry";



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
        RawResourceContainer rawResourceContainer = new RawResourceContainer();

        if (CollectionUtils.isEmpty(envoys)) throw new ApiPlaneException(ExceptionConst.GATEWAY_LIST_EMPTY);

        List<Endpoint> endpoints = resourceManager.getEndpointList();
        if (CollectionUtils.isEmpty(endpoints)) throw new ApiPlaneException(ExceptionConst.ENDPOINT_LIST_EMPTY);

        TemplateParams baseParams = initTemplateParams(api, namespace);

        rawResourceContainer.add(renderPlugins(api));

        List<String> rawGateways = buildGateways(api, envoys, baseParams);
        List<String> rawVirtualServices = buildVirtualServices(api, baseParams, endpoints, rawResourceContainer.getVirtualServices());
        List<String> rawDestinationRules = buildDestinationRules(api, baseParams);
        List<String> rawSharedConfigs = buildSharedConfigs(api, baseParams, rawResourceContainer.getSharedConfigs());

        List<String> rawResources = new ArrayList<>();
        rawResources.addAll(rawGateways);
        rawResources.addAll(rawVirtualServices);
        rawResources.addAll(rawDestinationRules);
        rawResources.addAll(rawSharedConfigs);

        logger.info("translated resources: ");
        rawResources.forEach(r -> logger.info(r));
        rawResources.stream()
                .forEach(r -> resources.add(str2IstioResource(r)));

        return resources;
    }

    public List<IstioResource> translate(PortalService service, String namespace) {

        List<IstioResource> resources = new ArrayList<>();

        List<String> destinations = buildDestinations(service, namespace);
        logger.info("translated resources: ");
        destinations.forEach(r -> logger.info(r));
        destinations.stream()
                .forEach(ds -> resources.add(str2IstioResource(ds)));

        return resources;
    }

    private IstioResource str2IstioResource(String str) {
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(str, ResourceType.YAML, editorContext);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        IstioResource ir = (IstioResource) gen.object(resourceEnum.mappingType());
        return ir;
    }

    /**
     * sharedconfig 全局唯一，仅创建一个
     *
     * @param api
     * @param baseParams
     * @param fragments
     * @return
     */
    private List<String> buildSharedConfigs(API api, TemplateParams baseParams, List<FragmentWrapper> fragments) {

        if (CollectionUtils.isEmpty(fragments)) return Collections.emptyList();

        List<String> descriptors = fragments.stream()
                .filter(f -> f != null)
                .map(f -> f.getContent())
                .collect(Collectors.toList());

        TemplateParams sharedConfigsParams = TemplateParams.instance()
                .setParent(baseParams)
                .put(SHARED_CONFIG_DESCRIPTOR, descriptors);

        return Arrays.asList(templateTranslator.translate(apiSharedConfig, sharedConfigsParams.output()));
    }


    private List<String> buildDestinationRules(API api, TemplateParams baseParams) {

        // 带porxyService的情况下，不需要创建destinationrule
        if (!CollectionUtils.isEmpty(api.getProxyServices())) return Collections.emptyList();
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
                    .put(DESTINATION_RULE_NAME, proxyUri);
            destinationRules.add(templateTranslator.translate(apiDestinationRule, destinationParams.output()));
        });
        return destinationRules;
    }

    /**
     * 创建DestinationRule or ServiceEntry
     * @param service
     * @param namespace
     * @return
     */
    private List<String> buildDestinations(PortalService service, String namespace) {
        List<String> destinations = new ArrayList<>();
        TemplateParams params = TemplateParams.instance()
                .put(DESTINATION_RULE_NAME, service.getCode().toLowerCase())
                .put(DESTINATION_RULE_HOST, service.getBackendService())
                .put(NAMESPACE, namespace)
                .put(API_SERVICE, service.getCode())
                .put(API_GATEWAY, service.getGateway());

        // host由服务类型决定，
        // 1. 当为动态时，则直接使用服务的后端地址，一般为httpbin.default.svc类似，
        // 2. 当为静态时，后端地址为IP或域名，使用服务的code作为host

        String host = service.getBackendService();
        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {

            host = service.getCode();
            //TODO translate service entry
            String backendService = service.getBackendService();

            destinations.add(templateTranslator.translate(serviceServiceEntry, params.output()));
        }
        params.put(DESTINATION_RULE_HOST, host);
        destinations.add(templateTranslator.translate(serviceDestinationRule, params.output()));
        return destinations;
    }

    private List<String> buildVirtualServices(API api, TemplateParams baseParams, List<Endpoint> endpoints, List<FragmentWrapper> fragments) {

        List<String> virtualservices = new ArrayList<>();
        // 插件分为match、api、host三个级别
        List<String> matchPlugins = new ArrayList<>();
        List<String> apiPlugins = new ArrayList<>();
        List<String> hostPlugins = new ArrayList<>();

        fragments.stream()
                .forEach(f -> {
                    if (f.getFragmentType().equals(FragmentTypeEnum.NEW_MATCH)) {
                        matchPlugins.add(f.getContent());
                    } else if (f.getFragmentType().equals(FragmentTypeEnum.DEFAULT_MATCH)) {
                        apiPlugins.add(f.getContent());
                    }
                });

        String matchYaml = produceMatch(baseParams);
        String httpApiYaml = produceHttpApi(baseParams);

        api.getGateways().stream().forEach(gw -> {
            String subset = buildVirtualServiceSubsetName(api.getService(), api.getName(), gw);
            String route = produceRoute(api, endpoints, subset);

            TemplateParams gatewayParams = TemplateParams.instance()
                    .setParent(baseParams)
                    .put(GATEWAY_NAME, buildGatewayName(api.getService(), gw))
                    .put(VIRTUAL_SERVICE_NAME, buildVirtualServiceName(api.getService(), gw))
                    .put(VIRTUAL_SERVICE_SUBSET_NAME, subset)
                    .put(VIRTUAL_SERVICE_MATCH_YAML, matchYaml)
                    .put(VIRTUAL_SERVICE_ROUTE_YAML, route)
                    .put(VIRTUAL_SERVICE_API_YAML, httpApiYaml)
                    .put(API_MATCH_PLUGINS, matchPlugins)
                    .put(API_API_PLUGINS, apiPlugins)
                    .put(API_HOST_PLUGINS, hostPlugins);

            // 根据api高级功能生成extra部分，该部分为所有match通用的部分
            String extraYaml = productExtra(gatewayParams);
            if (!StringUtils.isEmpty(extraYaml)) gatewayParams.put(VIRTUAL_SERVICE_EXTRA_YAML, extraYaml);
            Map<String, Object> mergedParams = gatewayParams.output();
            //先基础渲染
            String tempVs = templateTranslator.translate(apiVirtualService, mergedParams);
            //二次渲染插件中的内容
            String rawVs = templateTranslator.translate("tempVs", tempVs, mergedParams);
            virtualservices.add(adjustVs(rawVs));
        });
        return virtualservices;
    }

    private String adjustVs(String rawVs) {
        ResourceGenerator gen = ResourceGenerator.newInstance(rawVs, ResourceType.YAML);
        gen.removeElement("$.spec.http[?].route", Criteria.where("redirect").exists(true));
        gen.removeElement("$.spec.http[?].fault", Criteria.where("redirect").exists(true));
        return gen.yamlString();
    }

    private List<FragmentHolder> renderPlugins(API api) {

        if (CollectionUtils.isEmpty(api.getPlugins())) return Collections.emptyList();
        List<String> plugins = api.getPlugins().stream()
                                .filter(p -> !StringUtils.isEmpty(p))
                                .collect(Collectors.toList());
        api.setPlugins(plugins);
        ServiceInfo service = new ServiceInfo();
        service.setApiName(wrap(API_NAME));
        service.setUri(wrap(API_REQUEST_URIS));
        service.setMethod(wrap(API_METHODS));
        service.setSubset(wrap(VIRTUAL_SERVICE_SUBSET_NAME));
        service.setApi(api);

        List<FragmentHolder> fragments = plugins.stream()
                .map(p -> pluginService.processSchema(p, service))
                .collect(Collectors.toList());
        return fragments;
    }

    private List<String> buildGateways(API api, List<String> envoys, TemplateParams baseParams) {

        List<String> gateways = new ArrayList<>();
        envoys.stream().forEach(gw -> {
            TemplateParams gatewayParams = TemplateParams.instance()
                    .setParent(baseParams)
                    .put(API_GATEWAY, gw)
                    .put(GATEWAY_NAME, buildGatewayName(api.getService(), gw));

            gateways.add(templateTranslator.translate(apiGateway, gatewayParams.output()));
        });
        return gateways;
    }

    /**
     * 初始化渲染所需的基本参数
     *
     * @param api
     * @param namespace
     * @return
     */
    private TemplateParams initTemplateParams(API api, String namespace) {

        String uris = getUris(api);
        String methods = String.join("|", api.getMethods());
        String hosts = String.join("|", api.getHosts());

        TemplateParams tp = TemplateParams.instance()
                .put(NAMESPACE, namespace)
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
        return tp;
    }

    private String getUris(API api) {

        final StringBuffer suffix = new StringBuffer();
        if (api.getUriMatch().equals(UriMatch.PREFIX)) {
            suffix.append(".*");
        }
        return String.join("|", api.getRequestUris().stream()
                                            .map(u -> u + suffix.toString())
                                            .collect(Collectors.toList()));
    }

    /**
     * 合并两个crd,新的和旧的重叠部分会用新的覆盖旧的
     *
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
        return operator.subtract(old, service, api);
    }

    public boolean isUseless(IstioResource i) {
        return operator.isUseless(i);
    }

    private String wrap(String raw) {
        if (StringUtils.isEmpty(raw)) throw new NullPointerException();
        return "${" + raw + "}";
    }

    private String productExtra(TemplateParams params) {
        return templateTranslator.translate(apiVirtualServiceExtra, params.output());
    }

    private String produceMatch(TemplateParams params) {
        return templateTranslator.translate(apiVirtualServiceMatch, params.output());
    }

    private String produceHttpApi(TemplateParams params) {
        return templateTranslator.translate(apiVirtualServiceApi, params.output());
    }

//    private String produceHosts(TemplateParams params) {
//        return templateTranslator.translate(baseVirtualServiceHosts, params.output());
//    }


    private String produceRoute(API api, List<Endpoint> endpoints, String subset) {
        List<Map<String, Object>> destinations = new ArrayList<>();
        List<String> proxies = api.getProxyUris();

        // 适配带权重+动态静态的后端服务
        if (!CollectionUtils.isEmpty(api.getProxyServices())) {

            //only one gateway
            List<String> gateways = api.getGateways();
            String gateway = gateways.get(0);

            for (Service service : api.getProxyServices()) {

                Map<String, Object> param = new HashMap<>();
                param.put("weight", service.getWeight());

                param.put("subset", service.getCode() + "-" + gateway);

                Integer port = -1;
                String host = service.getCode();
                String addr = service.getBackendService();

                if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {
                    if (CommonUtil.isValidIPAddr(service.getBackendService())) {
                        port = Integer.valueOf(addr.split(":")[1]);
                    } else {
                        port = 80;
                    }
                } else if (Const.PROXY_SERVICE_TYPE_DYNAMIC.equals(service.getType())) {
                    for (Endpoint e : endpoints) {
                        if (e.getHostname().equals(service.getBackendService())) {
                            port = e.getPort();
                            host = service.getBackendService();
                            break;
                        }
                    }
                }

                if (port == -1) throw new ApiPlaneException(String.format("%s:%s", ExceptionConst.TARGET_SERVICE_NON_EXIST, service.getBackendService()));

                param.put("port", port);
                param.put("host", host);
                destinations.add(param);
            }

        // 纯动态后端服务
        } else {
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
        }

        String destinationStr = templateTranslator
                .translate(apiVirtualServiceRoute,
                        ImmutableMap.of(VIRTUAL_SERVICE_DESTINATIONS, destinations));
        return destinationStr;
    }


    private String buildGatewayName(String serviceName, String gw) {
        return gw;
    }

    private String buildVirtualServiceName(String serviceName, String gw) {
        return String.format("%s-%s", serviceName, gw);
    }

    private String buildVirtualServiceSubsetName(String serviceName, String apiName, String gw) {
        return String.format("%s-%s-%s", serviceName, apiName, gw);
    }
}
