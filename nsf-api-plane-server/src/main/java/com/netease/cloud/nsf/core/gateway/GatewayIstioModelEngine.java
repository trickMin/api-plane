package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.core.GlobalConfig;
import com.netease.cloud.nsf.core.IstioModelEngine;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.gateway.handler.*;
import com.netease.cloud.nsf.core.gateway.processor.DefaultModelProcessor;
import com.netease.cloud.nsf.core.gateway.processor.NeverReturnNullModelProcessor;
import com.netease.cloud.nsf.core.gateway.processor.RenderTwiceModelProcessor;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.core.k8s.K8sResourcePack;
import com.netease.cloud.nsf.core.k8s.empty.EmptyConfigMap;
import com.netease.cloud.nsf.core.k8s.merger.RateLimitConfigMapMerger;
import com.netease.cloud.nsf.core.k8s.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.k8s.subtracter.RateLimitConfigMapSubtracter;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.Const;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
@Component
public class GatewayIstioModelEngine extends IstioModelEngine {

    private static final Logger logger = LoggerFactory.getLogger(IstioModelEngine.class);

    private IntegratedResourceOperator operator;
    private TemplateTranslator templateTranslator;
    private EditorContext editorContext;
    private ResourceManager resourceManager;
    private PluginService pluginService;
    private GlobalConfig globalConfig;

    @Autowired
    public GatewayIstioModelEngine(IntegratedResourceOperator operator, TemplateTranslator templateTranslator, EditorContext editorContext,
                            ResourceManager resourceManager, PluginService pluginService, GlobalConfig globalConfig) {
        super(operator);
        this.operator = operator;
        this.templateTranslator = templateTranslator;
        this.editorContext = editorContext;
        this.resourceManager = resourceManager;
        this.pluginService = pluginService;
        this.globalConfig = globalConfig;

        this.defaultModelProcessor = new DefaultModelProcessor(templateTranslator);
        this.renderTwiceModelProcessor = new RenderTwiceModelProcessor(templateTranslator);
        this.neverNullRenderTwiceProcessor = new NeverReturnNullModelProcessor(this.renderTwiceModelProcessor, NEVER_NULL);
    }

    private DefaultModelProcessor defaultModelProcessor;
    private RenderTwiceModelProcessor renderTwiceModelProcessor;
    private NeverReturnNullModelProcessor neverNullRenderTwiceProcessor;

    @Value(value = "${http10:#{null}}")
    Boolean enableHttp10;

    @Value(value = "${rateLimitConfigMapName:rate-limit-config}")
    String rateLimitConfigMapName;

    private static final String NEVER_NULL = "NEVER_NULL";

    private static final String apiGateway = "gateway/api/gateway";
    private static final String apiVirtualService = "gateway/api/virtualService";
    private static final String apiDestinationRule = "gateway/api/destinationRule";
    private static final String apiSharedConfigConfigMap = "gateway/api/sharedConfigConfigMap";

    private static final String serviceDestinationRule = "gateway/service/destinationRule";
    private static final String pluginManager = "gateway/pluginManager";
    private static final String serviceServiceEntry = "gateway/service/serviceEntry";
    private static final String globalGatewayPlugin = "gateway/globalGatewayPlugin";
    private static final String gatewayPlugin = "gateway/gatewayPlugin";

    public List<K8sResourcePack> translate(API api) {
        return translate(api, false);
    }

    /**
     * 将api转换为istio对应的规则
     *
     * @param api
     * @param simple 是否为简单模式，部分字段不渲染，主要用于删除
     * @return
     */
    public List<K8sResourcePack> translate(API api, boolean simple) {

        List<K8sResourcePack> resourcePacks = new ArrayList<>();

        RawResourceContainer rawResourceContainer = new RawResourceContainer();
        rawResourceContainer.add(renderPlugins(api));

        List<String> extraDestination = CollectionUtils.isEmpty(api.getPlugins()) ?
                Collections.emptyList() : pluginService.extractService(api.getPlugins());
        BaseVirtualServiceAPIDataHandler vsHandler;

        if (CollectionUtils.isEmpty(api.getProxyServices())) {
            //yx
            List<Endpoint> endpoints = resourceManager.getEndpointList();
            vsHandler = new YxVirtualServiceAPIDataHandler(
                    defaultModelProcessor, rawResourceContainer.getVirtualServices(), endpoints, simple);
            List<String> rawGateways = defaultModelProcessor.process(apiGateway, api, new BaseGatewayAPIDataHandler(enableHttp10));
            resourcePacks.addAll(generateK8sPack(rawGateways));
            List<String> rawDestinationRules = defaultModelProcessor.process(apiDestinationRule, api, new BaseDestinationRuleAPIDataHandler(extraDestination));
            resourcePacks.addAll(generateK8sPack(rawDestinationRules));
        } else {
            //gportal
            vsHandler = new PortalVirtualServiceAPIDataHandler(
                    defaultModelProcessor, rawResourceContainer.getVirtualServices(), simple);
        }

        List<String> rawVirtualServices = renderTwiceModelProcessor.process(apiVirtualService, api, vsHandler);
        List<String> rawSharedConfigs = neverNullRenderTwiceProcessor.process(apiSharedConfigConfigMap, api, new BaseSharedConfigAPIDataHandler(rawResourceContainer.getSharedConfigs(), rateLimitConfigMapName));
        // vs上的插件转移到gatewayplugin上
        List<String> rawGatewayPlugins = renderTwiceModelProcessor.process(gatewayPlugin, api,
                new ApiGatewayPluginDataHandler(rawResourceContainer.getVirtualServices(), globalConfig.getResourceNamespace()));

        resourcePacks.addAll(generateK8sPack(rawVirtualServices, r -> r, this::adjust));
        // rate limit configmap
        resourcePacks.addAll(generateK8sPack(rawSharedConfigs,
                new RateLimitConfigMapMerger(),
                new RateLimitConfigMapSubtracter(String.join("|", api.getGateways()), api.getName()),
                new GatewayIstioModelEngine.EmptyResourceGenerator(new EmptyConfigMap(rateLimitConfigMapName))));
        resourcePacks.addAll(generateK8sPack(rawGatewayPlugins));

        return resourcePacks;
    }

    public List<K8sResourcePack> translate(Service service) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> destinations = defaultModelProcessor.process(serviceDestinationRule, service, new PortalDestinationRuleServiceDataHandler());
        resources.addAll(generateK8sPack(destinations));
        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {
            List<String> serviceEntries = defaultModelProcessor.process(serviceServiceEntry, service, new PortalServiceEntryServiceDataHandler());
            resources.addAll(generateK8sPack(serviceEntries));
        }
        return resources;
    }

    /**
     * 将gateway转换为istio对应的规则
     *
     * @param istioGateway
     * @return
     */
    public List<K8sResourcePack> translate(IstioGateway istioGateway) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> rawGateways = defaultModelProcessor.process(apiGateway, istioGateway, new PortalGatewayDataHandler(enableHttp10));
        resources.addAll(generateK8sPack(rawGateways));
        return resources;
    }

    public List<K8sResourcePack> translate(PluginOrder po) {

        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> pluginManagers = defaultModelProcessor.process(pluginManager, po, new PluginOrderDataHandler());
        resources.addAll(generateK8sPack(pluginManagers));
        return resources;
    }

    public List<K8sResourcePack> translate(GlobalPlugin gp) {

        List<K8sResourcePack> resources = new ArrayList<>();
        RawResourceContainer rawResourceContainer = new RawResourceContainer();
        List<FragmentHolder> plugins = pluginService.processGlobalPlugin(gp.getPlugins(), new ServiceInfo());
        rawResourceContainer.add(plugins);
        List<Gateway> gateways = resourceManager.getGatewayList();

        List<String> rawGatewayPlugins = defaultModelProcessor.process(globalGatewayPlugin, gp,
                new GatewayPluginDataHandler(rawResourceContainer.getGatewayPlugins(), gateways));
        //todo: shareConfig逻辑需要适配
        List<String> rawSharedConfigs = renderTwiceModelProcessor.process(apiSharedConfigConfigMap, gp,
                new GatewayPluginSharedConfigDataHandler(rawResourceContainer.getSharedConfigs(), gateways, rateLimitConfigMapName));
        resources.addAll(generateK8sPack(rawGatewayPlugins));
        resources.addAll(generateK8sPack(rawSharedConfigs,
                new RateLimitConfigMapMerger(),
                new RateLimitConfigMapSubtracter(gp.getGateway(), gp.getCode()),
                new GatewayIstioModelEngine.EmptyResourceGenerator(new EmptyConfigMap(rateLimitConfigMapName))));

        return resources;
    }

    private List<FragmentHolder> renderPlugins(API api) {

        if (CollectionUtils.isEmpty(api.getPlugins())) return Collections.emptyList();
        List<String> plugins = api.getPlugins().stream()
                .filter(p -> !StringUtils.isEmpty(p))
                .collect(Collectors.toList());
        api.setPlugins(plugins);

        return pluginService.processPlugin(plugins, new ServiceInfo());
    }

    private HasMetadata adjust(HasMetadata rawVs) {
        if ("VirtualService".equalsIgnoreCase(rawVs.getKind())) {
            VirtualService vs = (VirtualService) rawVs;
            List<HTTPRoute> routes = Optional.ofNullable(vs.getSpec().getHttp()).orElse(new ArrayList<>());
            routes.forEach(route -> {
                if (Objects.nonNull(route.getReturn())) route.setRoute(null);
                if (Objects.nonNull(route.getRedirect())) route.setRoute(null);
                if (Objects.nonNull(route.getRedirect())) route.setFault(null);
            });
        }
        return rawVs;
    }

    private class EmptyResourceGenerator implements Function<String, HasMetadata> {

        private HasMetadata hmd;

        public EmptyResourceGenerator(HasMetadata hmd) {
            this.hmd = hmd;
        }

        @Override
        public HasMetadata apply(String s) {
            if (NEVER_NULL.equals(s)) return hmd;
            return str2HasMetadata(s);
        }
    }

}
