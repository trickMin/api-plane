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
import com.netease.cloud.nsf.core.k8s.empty.DynamicGatewayPluginSupplier;
import com.netease.cloud.nsf.core.k8s.empty.EmptyConfigMap;
import com.netease.cloud.nsf.core.k8s.merger.GatewayRateLimitConfigMapMerger;
import com.netease.cloud.nsf.core.k8s.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.k8s.subtracter.GatewayPluginNormalSubtracter;
import com.netease.cloud.nsf.core.k8s.subtracter.GatewayRateLimitConfigMapSubtracter;
import com.netease.cloud.nsf.core.k8s.subtracter.GatewayVirtualServiceSubtracter;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.constant.PluginConstant;
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

    @Value(value = "${rateLimitNameSpace:gateway-system}")
    String rateLimitNamespace;


    private static final String apiGateway = "gateway/api/gateway";
    private static final String apiVirtualService = "gateway/api/virtualService";
    private static final String rateLimitConfigMap = "gateway/api/rateLimitConfigMap";

    private static final String serviceDestinationRule = "gateway/service/destinationRule";
    private static final String pluginManager = "gateway/pluginManager";
    private static final String serviceServiceEntry = "gateway/service/serviceEntry";
    private static final String gatewayPlugin = "gateway/gatewayPlugin";

    public List<K8sResourcePack> translate(API api) {
        return translate(api, false);
    }

    /**
     * 将api转换为istio对应的规则
     *
     * @param api 路由信息
     * @param simple 是否为简单模式，部分字段不渲染，主要用于删除
     * @return K8s资源集合
     */
    public List<K8sResourcePack> translate(API api, boolean simple) {
        logger.info("[translate CRDs][route API] start translate k8s resource");
        List<K8sResourcePack> resourcePacks = new ArrayList<>();
        List<FragmentWrapper> vsFragments = new ArrayList<>();

        // 渲染VS上的插件片段（当前仅有Match插件）
        if (!StringUtils.isEmpty(api.getPlugins())) {
            vsFragments = renderPlugins(api.getPlugins()).stream()
                    .map(FragmentHolder::getVirtualServiceFragment)
                    .collect(Collectors.toList());
        }
        List<String> rawVirtualServices = renderTwiceModelProcessor
                .process(apiVirtualService, api,
                        new PortalVirtualServiceAPIDataHandler(
                                defaultModelProcessor, vsFragments, simple));

        logger.info("[translate CRDs][route API] start to generate and add k8s resource");
        resourcePacks.addAll(generateK8sPack(rawVirtualServices,
                new GatewayVirtualServiceSubtracter(api.getName()),
                r -> r, this::adjust));
        logger.info("[translate CRDs][route API] raw virtual services added ok");

        return resourcePacks;
    }

    /**
     * 转换GatewayPlugin数据为CRD资源
     *
     * @param plugin 网关插件实例（内含插件配置）
     * @return k8s资源集合
     */
    public List<K8sResourcePack> translate(GatewayPlugin plugin) {
        logger.info("[translate CRDs][gateway plugin] start translate k8s resource");

        // 打印插件配置信息
        plugin.showPluginConfigsInLog(logger);

        RawResourceContainer rawResourceContainer = new RawResourceContainer();
        rawResourceContainer.add(renderPlugins(plugin.getPlugins()));

        logger.info("[translate CRDs][gateway plugin] render plugins ok, start to generate and add k8s resource");
        return generateAndAddK8sResource(rawResourceContainer, plugin);
    }

    /**
     * 生成k8s资源并将其合并为k8s资源集合
     *
     * @param rawResourceContainer k8s资源片段存放容器实例
     * @param plugin 网关插件实例（内含插件配置）
     * @return k8s资源集合
     */
    private List<K8sResourcePack> generateAndAddK8sResource(RawResourceContainer rawResourceContainer,
                                                            GatewayPlugin plugin) {
        // 插件渲染GatewayPlugin资源
        logger.info("[translate CRDs][gateway plugin] start render raw GatewayPlugin CRDs");
        List<K8sResourcePack> resourcePacks = configureGatewayPlugin(rawResourceContainer, plugin);

        // 路由级别的限流插件要渲染ConfigMap资源
        if (isNeedToRenderConfigMap(plugin)) {
            logger.info("[translate CRDs][gateway plugin] start render raw ConfigMap CRDs");
            configureRateLimitConfigMap(rawResourceContainer, resourcePacks, plugin);
        }

        return resourcePacks;
    }

    /**
     * 插件流程只有两个场景需要渲染ConfigMap资源
     * 1.路由级别的限流插件
     * 2.没有传插件类型，即批量操作的场景，此时可能会有限流插件，因此需要无差别渲染（匹配路由启用、禁用、下线场景）
     * 注：限流插件只有路由级别，因此路由插件是先决条件
     *
     * @param plugin 网关插件实例（内含插件配置）
     * @return 是否需要渲染ConfigMap资源
     */
    private boolean isNeedToRenderConfigMap(GatewayPlugin plugin) {
        return plugin.isRoutePlugin() &&
                (StringUtils.isEmpty(plugin.getPluginType()) || plugin.getPluginType().equals(PluginConstant.RATE_LIMIT_PLUGIN_TYPE));
    }

    /**
     * 插件转换为GatewayPlugin CRD资源
     *
     * @param rawResourceContainer 存放资源的容器对象
     * @param plugin 插件对象
     * @return k8s资源集合
     */
    private List<K8sResourcePack> configureGatewayPlugin(RawResourceContainer rawResourceContainer,
                                                         GatewayPlugin plugin) {
        List<K8sResourcePack> resourcePacks = new ArrayList<>();
        // 插件配置放在GatewayPlugin的CRD上
        List<String> rawGatewayPlugins = renderTwiceModelProcessor.process(gatewayPlugin, plugin,
                new GatewayPluginDataHandler(
                        rawResourceContainer.getVirtualServices(), globalConfig.getResourceNamespace()));

        // 当插件传入为空时，生成空的GatewayPlugin，删除时使用
        DynamicGatewayPluginSupplier dynamicGatewayPluginSupplier =
                new DynamicGatewayPluginSupplier(plugin.getGateway(), plugin.getRouteId(), "%s-%s");

        resourcePacks.addAll(generateK8sPack(rawGatewayPlugins,
                null,
                new GatewayPluginNormalSubtracter(),
                new DynamicResourceGenerator(dynamicGatewayPluginSupplier)));
        logger.info("[translate CRDs][gateway plugin] raw GatewayPlugin CRDs added ok");

        return resourcePacks;
    }

    /**
     * 限流插件转换为ConfigMap CRD资源
     *
     * @param rawResourceContainer 存放资源的容器对象
     * @param resourcePacks k8s资源集合
     * @param plugin 插件对象
     */
    private void configureRateLimitConfigMap(RawResourceContainer rawResourceContainer,
                                             List<K8sResourcePack> resourcePacks,
                                             GatewayPlugin plugin) {
        // 限流插件需要额外的configMap配置
        List<String> rawConfigMaps = neverNullRenderTwiceProcessor.process(rateLimitConfigMap, plugin,
                new GatewayPluginConfigMapDataHandler(
                        rawResourceContainer.getSharedConfigs(), rateLimitConfigMapName, rateLimitNamespace));
        // 加入限流插件configMap配置
        resourcePacks.addAll(generateK8sPack(rawConfigMaps,
                new GatewayRateLimitConfigMapMerger(),
                new GatewayRateLimitConfigMapSubtracter(plugin.getGateway(), plugin.getRouteId()),
                new EmptyResourceGenerator(new EmptyConfigMap(rateLimitConfigMapName, rateLimitNamespace))));
        logger.info("[translate CRDs][gateway plugin] raw ConfigMap CRDs added ok");
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

    private List<FragmentHolder> renderPlugins(List<String> pluginList) {

        if (CollectionUtils.isEmpty(pluginList)) {
            return Collections.emptyList();
        }

        List<String> plugins = pluginList.stream()
                .filter(p -> !StringUtils.isEmpty(p))
                .collect(Collectors.toList());

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

}
