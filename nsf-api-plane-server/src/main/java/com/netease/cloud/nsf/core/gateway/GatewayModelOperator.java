package com.netease.cloud.nsf.core.gateway;

import com.jayway.jsonpath.Criteria;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.handler.*;
import com.netease.cloud.nsf.core.gateway.processor.DefaultModelProcessor;
import com.netease.cloud.nsf.core.gateway.processor.RenderTwiceModelProcessor;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.core.istio.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.service.PluginService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;


/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
@Component
public class GatewayModelOperator {

    private static final Logger logger = LoggerFactory.getLogger(GatewayModelOperator.class);

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
    RenderTwiceModelProcessor renderTwiceModelProcessor;

    @Autowired
    DefaultModelProcessor defaultModelProcessor;

    private static final String apiGateway = "gateway/api/gateway";
    private static final String apiVirtualService = "gateway/api/virtualService";
    private static final String apiDestinationRule = "gateway/api/destinationRule";
    private static final String apiSharedConfig = "gateway/api/sharedConfig";

    private static final String serviceDestinationRule = "gateway/service/destinationRule";

    private static final String pluginManager = "gateway/pluginManager";

    private static final String serviceServiceEntry = "gateway/service/serviceEntry";

    /**
     * 将api转换为istio对应的规则
     *
     * @param api
     * @param namespace
     * @return
     */
    public List<IstioResource> translate(API api, String namespace) {

        api.setNamespace(namespace);
        List<Endpoint> endpoints = resourceManager.getEndpointList();
        if (CollectionUtils.isEmpty(endpoints)) throw new ApiPlaneException(ExceptionConst.ENDPOINT_LIST_EMPTY);

        List<IstioResource> resources = new ArrayList<>();

        RawResourceContainer rawResourceContainer = new RawResourceContainer();
        rawResourceContainer.add(renderPlugins(api));

        List<String> extraDestination = CollectionUtils.isEmpty(api.getPlugins()) ?
                Collections.emptyList() : pluginService.extractService(api.getPlugins());

        BaseVirtualServiceAPIDataHandler vsHandler;
        if (CollectionUtils.isEmpty(api.getProxyServices())) {
            vsHandler = new BaseVirtualServiceAPIDataHandler(
                    defaultModelProcessor, pluginService, rawResourceContainer.getVirtualServices(), endpoints);
        } else {
            vsHandler = new PortalVirtualServiceAPIDataHandler(
                    defaultModelProcessor, pluginService, rawResourceContainer.getVirtualServices(), endpoints);
        }

        List<String> rawVirtualServices = renderTwiceModelProcessor.process(apiVirtualService, api, vsHandler);
        List<String> rawGateways = defaultModelProcessor.process(apiGateway, api, new BaseGatewayAPIDataHandler());
        List<String> rawDestinationRules = defaultModelProcessor.process(apiDestinationRule, api, new BaseDestinationRuleAPIDataHandler(extraDestination));
        List<String> rawSharedConfigs = defaultModelProcessor.process(apiSharedConfig, api, new BaseSharedConfigAPIDataHandler(rawResourceContainer.getSharedConfigs()));

        List<String> rawResources = new ArrayList<>();
        rawResources.addAll(rawGateways);
        rawResources.addAll(rawVirtualServices.stream().map(vs -> adjustVs(vs)).collect(Collectors.toList()));
        rawResources.addAll(rawDestinationRules);
        rawResources.addAll(rawSharedConfigs);

        rawResources.stream()
                .forEach(r -> resources.add(str2IstioResource(r)));

        return resources;
    }

    public List<IstioResource> translate(Service service, String namespace) {

        List<IstioResource> resources = new ArrayList<>();
        service.setNamespace(namespace);
        List<String> destinations = defaultModelProcessor.process(serviceDestinationRule, service, new PortalDestinationRuleServiceDataHandler());
        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {
            destinations.addAll(defaultModelProcessor.process(serviceServiceEntry, service, new PortalServiceEntryServiceDataHandler()));
        }
        destinations.stream()
                .forEach(ds -> resources.add(str2IstioResource(ds)));

        return resources;
    }

    public List<IstioResource> translate(PluginOrder po, String namespace) {

        List<IstioResource> resources = new ArrayList<>();
        po.setNamespace(namespace);
        List<String> pluginManagers = defaultModelProcessor.process(pluginManager, po, new PluginOrderDataHandler());
        pluginManagers.stream()
                .forEach(ds -> resources.add(str2IstioResource(ds)));
        return resources;
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
     */
    public IstioResource subtract(IstioResource old, Map<String, String> values) {
        return operator.subtract(old, values.get(old.getKind()));
    }

    public boolean isUseless(IstioResource i) {
        return operator.isUseless(i);
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

        List<FragmentHolder> fragments = pluginService.processSchema(plugins, service);
        return fragments;
    }

    private String wrap(String raw) {
        if (StringUtils.isEmpty(raw)) throw new NullPointerException();
        return "${" + raw + "}";
    }

    private IstioResource str2IstioResource(String str) {

        logger.info("raw resource: " + str);
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(str, ResourceType.YAML, editorContext);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        IstioResource ir = (IstioResource) gen.object(resourceEnum.mappingType());
        return ir;
    }
}
