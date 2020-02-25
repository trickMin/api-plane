package com.netease.cloud.nsf.core.gateway;

import com.jayway.jsonpath.Criteria;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.handler.*;
import com.netease.cloud.nsf.core.gateway.processor.DefaultModelProcessor;
import com.netease.cloud.nsf.core.gateway.processor.RenderTwiceModelProcessor;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourcePack;
import com.netease.cloud.nsf.core.k8s.merger.RateLimitConfigMapMerger;
import com.netease.cloud.nsf.core.k8s.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.k8s.subtracter.RateLimitConfigMapSubtracter;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import com.netease.cloud.nsf.util.function.Merger;
import com.netease.cloud.nsf.util.function.Subtracter;
import io.fabric8.kubernetes.api.model.HasMetadata;
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
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
@Component
public class IstioModelProcessor {

    private static final Logger logger = LoggerFactory.getLogger(IstioModelProcessor.class);

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

    @Autowired
    GatewayService gatewayService;

    @Autowired
    RenderTwiceModelProcessor renderTwiceModelProcessor;

    @Autowired
    DefaultModelProcessor defaultModelProcessor;

    @Value(value = "${http10:#{null}}")
    Boolean enableHttp10;

    @Value(value = "${sharedConfigName:rate-limit-config}")
    String sharedConfigName;

    private static final String apiGateway = "gateway/api/gateway";
    private static final String apiVirtualService = "gateway/api/virtualService";
    private static final String apiDestinationRule = "gateway/api/destinationRule";
    private static final String apiSharedConfigConfigMap = "gateway/api/sharedConfigConfigMap";

    private static final String serviceDestinationRule = "gateway/service/destinationRule";
    private static final String pluginManager = "gateway/pluginManager";
    private static final String serviceServiceEntry = "gateway/service/serviceEntry";
    private static final String gatewayPlugin = "gateway/gatewayPlugin";

    private static final String versionManager = "sidecarVersionManagement";

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

        List<Endpoint> endpoints = resourceManager.getEndpointList();

        List<K8sResourcePack> resourcePacks = new ArrayList<>();

        RawResourceContainer rawResourceContainer = new RawResourceContainer();
        rawResourceContainer.add(renderPlugins(api));

        List<String> extraDestination = CollectionUtils.isEmpty(api.getPlugins()) ?
                Collections.emptyList() : pluginService.extractService(api.getPlugins());
        BaseVirtualServiceAPIDataHandler vsHandler;

        if (CollectionUtils.isEmpty(api.getProxyServices())) {
            //yx
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
        List<String> rawSharedConfigs = renderTwiceModelProcessor.process(apiSharedConfigConfigMap, api, new BaseSharedConfigAPIDataHandler(rawResourceContainer.getSharedConfigs(), sharedConfigName));

        resourcePacks.addAll(generateK8sPack(rawVirtualServices, vs -> adjustVs(vs)));
        resourcePacks.addAll(generateK8sPack(rawSharedConfigs,
                new RateLimitConfigMapMerger(), new RateLimitConfigMapSubtracter(api.getName())));

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

    public List<K8sResourcePack> translate(SidecarVersionManagement svm) {

        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> versionManagers = defaultModelProcessor.process(versionManager, svm, new VersionManagersDataHandler());
        resources.addAll(generateK8sPack(Arrays.asList(versionManagers.get(0))));
        return resources;
    }

    public List<K8sResourcePack> translate(GlobalPlugin gp) {

        List<K8sResourcePack> resources = new ArrayList<>();
        RawResourceContainer rawResourceContainer = new RawResourceContainer();
        List<FragmentHolder> plugins = pluginService.processGlobalPlugin(gp.getPlugins(), new ServiceInfo());
        rawResourceContainer.add(plugins);
        List<Gateway> gateways = gatewayService.getGatewayList();

        List<String> rawGatewayPlugins = defaultModelProcessor.process(gatewayPlugin, gp, new GatewayPluginDataHandler(rawResourceContainer.getGatewayPlugins(), gateways));
        //todo: shareConfig逻辑需要适配
        List<String> rawSharedConfigs = renderTwiceModelProcessor.process(apiSharedConfigConfigMap, gp,
                new GatewayPluginSharedConfigDataHandler(rawResourceContainer.getSharedConfigs(), gateways, sharedConfigName));
        resources.addAll(generateK8sPack(rawGatewayPlugins));
        resources.addAll(generateK8sPack(rawSharedConfigs,
                new RateLimitConfigMapMerger(), new RateLimitConfigMapSubtracter(gp.getCode())));

        return resources;
    }

    /**
     * 合并两个crd,新的和旧的重叠部分会用新的覆盖旧的
     *
     * @param old
     * @param fresh
     * @return
     */
    public HasMetadata merge(HasMetadata old, HasMetadata fresh) {

        if (fresh == null) return old;
        if (old == null) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        return operator.merge(old, fresh);
    }

    /**
     * 在已有的istio crd中删去对应api部分
     */
    public HasMetadata subtract(HasMetadata old, Map<String, String> values) {
        return operator.subtract(old, values.get(old.getKind()));
    }

    public boolean isUseless(HasMetadata i) {
        return operator.isUseless(i);
    }

    private String adjustVs(String rawVs) {
        ResourceGenerator gen = ResourceGenerator.newInstance(rawVs, ResourceType.YAML);
        gen.removeElement("$.spec.http[?].route", Criteria.where("return").exists(true));
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

        return pluginService.processPlugin(plugins, new ServiceInfo());
    }

    private HasMetadata str2HasMetadata(String str) {

        logger.info("raw resource: " + str);
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(str, ResourceType.YAML);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        HasMetadata hmd = gen.object(resourceEnum.mappingType());
        return hmd;
    }

    private List<K8sResourcePack> generateK8sPack(List<String> raws) {
        return generateK8sPack(raws, null, null, r -> r);
    }

    private List<K8sResourcePack> generateK8sPack(List<String> raws, Merger merger, Subtracter subtracter) {
        return generateK8sPack(raws, merger, subtracter, r -> r);
    }

    private List<K8sResourcePack> generateK8sPack(List<String> raws, Function<String, String> preFun) {
        return generateK8sPack(raws, null, null, preFun);
    }

    private List<K8sResourcePack> generateK8sPack(List<String> raws, Merger merger, Subtracter subtracter,
                                                  Function<String, String> preFun) {
        if (CollectionUtils.isEmpty(raws)) return Collections.EMPTY_LIST;

        return raws.stream().map(r -> preFun.apply(r))
                .map(r -> str2HasMetadata(r))
                .map(hsm -> new K8sResourcePack(hsm, merger, subtracter))
                .collect(Collectors.toList());
    }
}