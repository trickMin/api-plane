package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.IstioModelEngine;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.gateway.handler.VersionManagersDataHandler;
import com.netease.cloud.nsf.core.gateway.processor.DefaultModelProcessor;
import com.netease.cloud.nsf.core.gateway.processor.NeverReturnNullModelProcessor;
import com.netease.cloud.nsf.core.gateway.processor.RenderTwiceModelProcessor;
import com.netease.cloud.nsf.core.k8s.K8sResourcePack;
import com.netease.cloud.nsf.core.k8s.empty.EmptyConfigMap;
import com.netease.cloud.nsf.core.k8s.empty.EmptyGatewayPlugin;
import com.netease.cloud.nsf.core.k8s.empty.EmptySmartLimiter;
import com.netease.cloud.nsf.core.k8s.merger.CircuitConfigMapMerger;
import com.netease.cloud.nsf.core.k8s.merger.MeshRateLimitConfigMapMerger;
import com.netease.cloud.nsf.core.k8s.merger.MeshRateLimitGatewayPluginMerger;
import com.netease.cloud.nsf.core.k8s.merger.SmartLimiterMerger;
import com.netease.cloud.nsf.core.k8s.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.k8s.subtracter.MeshRateLimitConfigMapSubtracter;
import com.netease.cloud.nsf.core.k8s.subtracter.MeshRateLimitGatewayPluginSubtracter;
import com.netease.cloud.nsf.core.k8s.subtracter.SmartLimiterSubtracter;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.servicemesh.handler.CircuitBreakerDataHandler;
import com.netease.cloud.nsf.core.servicemesh.handler.GlobalShareRateLimitDataHandler;
import com.netease.cloud.nsf.core.servicemesh.handler.RateLimiterDataHandler;
import com.netease.cloud.nsf.core.template.TemplateConst;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
@Component
public class ServiceMeshIstioModelEngine extends IstioModelEngine {

    private static final Logger logger = LoggerFactory.getLogger(ServiceMeshIstioModelEngine.class);

    private DefaultModelProcessor defaultModelProcessor;
    private PluginService pluginService;
    private RenderTwiceModelProcessor renderTwiceModelProcessor;
    private NeverReturnNullModelProcessor neverNullRenderTwiceProcessor;

    private static final String versionManager = "sidecarVersionManagement";
    private static final String circuitGatewayPlugin = "mesh/gatewayPlugin";
    private static final String smartLimiter = "mesh/smartLimiter";
    private static final String gatewayPlugin = "mesh/globalGatewayPlugin";
    private static final String sidecar = "mesh/sidecar";
    private static final String rlsConfigMap = "mesh/rlsConfigMap";

    @Value(value = "${rateLimitConfigMapName:rate-limit-config}")
    String rateLimitConfigMapName;

    @Autowired
    public ServiceMeshIstioModelEngine(IntegratedResourceOperator operator, TemplateTranslator templateTranslator, PluginService pluginService) {
        super(operator);
        this.pluginService = pluginService;
        this.defaultModelProcessor = new DefaultModelProcessor(templateTranslator);
        this.renderTwiceModelProcessor = new RenderTwiceModelProcessor(templateTranslator);
        this.neverNullRenderTwiceProcessor = new NeverReturnNullModelProcessor(this.renderTwiceModelProcessor, NEVER_NULL);
    }

    public List<K8sResourcePack> translate(SidecarVersionManagement svm) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> versionManagers = defaultModelProcessor.process(versionManager, svm, new VersionManagersDataHandler());
        resources.addAll(generateK8sPack(Arrays.asList(versionManagers.get(0))));
        return resources;
    }

    public List<K8sResourcePack> translate(ServiceMeshRateLimit rateLimit) {

        String plugin = rateLimit.getPlugin();
        RateLimitPlugin globalSharePlugin = new RateLimitPlugin();
        RateLimitPlugin otherPlugin = new RateLimitPlugin();
        List<K8sResourcePack> resourcePacks = new ArrayList<>();

        if (!StringUtils.isEmpty(rateLimit.getPlugin())) {
            RateLimitPlugin rateLimitPlugin = ResourceGenerator.json2obj(plugin, RateLimitPlugin.class);
            if (rateLimitPlugin != null && !CollectionUtils.isEmpty(rateLimitPlugin.getRules())) {
                //redis全局和其他类型分开
                List<RateLimitPlugin.Rule> rules = rateLimitPlugin.getRules();
                //FIXME 使用网关的插件
                globalSharePlugin.setKind("ianus-rate-limiting");
                rules.forEach(r -> {
                    if (r.getType().equals("GlobalShare")) {
                        globalSharePlugin.getRules().add(r);
                    } else {
                        otherPlugin.getRules().add(r);
                    }
                });
            }
        }

        //处理全局redis限流
        ServiceMeshRateLimit globalRateLimit = CommonUtil.copy(rateLimit);
        String global = CollectionUtils.isEmpty(globalSharePlugin.getRules()) ? null : ResourceGenerator.obj2json(globalSharePlugin);
        globalRateLimit.setPlugin(global);
        resourcePacks.addAll(handleGlobalShareRateLimit(globalRateLimit));

        //处理非全局限流
        ServiceMeshRateLimit otherRateLimit = CommonUtil.copy(rateLimit);
        String other = CollectionUtils.isEmpty(otherPlugin.getRules()) ? null : ResourceGenerator.obj2json(otherPlugin);
        otherRateLimit.setPlugin(other);
        resourcePacks.addAll(handleOtherRateLimit(otherRateLimit));

        return resourcePacks;
    }

    /**
     * 处理全局限流(redis)
     * @param rateLimit
     * @return 生成gateway plugin + config map
     */
    private List<K8sResourcePack> handleGlobalShareRateLimit(ServiceMeshRateLimit rateLimit) {

        ServiceInfo serviceInfo = ServiceInfo.instance();
        serviceInfo.setServiceName(rateLimit.getHost());
        List<FragmentHolder> fragmentHolders = new ArrayList<>();
        if (!StringUtils.isEmpty(rateLimit.getPlugin())) {
            fragmentHolders = pluginService.processGlobalPlugin(Arrays.asList(rateLimit.getPlugin()), serviceInfo);
        }

        List<K8sResourcePack> resourcePacks = new ArrayList<>();

        List<TemplateParams> params = new GlobalShareRateLimitDataHandler(fragmentHolders, rateLimitConfigMapName).handle(rateLimit);
        List<String> rawGatewayPlugin = neverNullRenderTwiceProcessor.process(gatewayPlugin, params);
        List<String> rawConfigMap = neverNullRenderTwiceProcessor.process(rlsConfigMap, params);

        resourcePacks.addAll(generateK8sPack(rawConfigMap,
                new MeshRateLimitConfigMapMerger(),
                new MeshRateLimitConfigMapSubtracter(rateLimit.getHost()),
                new EmptyResourceGenerator(new EmptyConfigMap(rateLimitConfigMapName))));

        resourcePacks.addAll(generateK8sPack(rawGatewayPlugin,
                new MeshRateLimitGatewayPluginMerger(),
                new MeshRateLimitGatewayPluginSubtracter(),
                new EmptyResourceGenerator(new EmptyGatewayPlugin(rateLimit.getHost(), rateLimit.getNamespace()))));

        return resourcePacks;
    }

    /**
     * 处理非全局限流
     * @param rateLimit
     * @return 生成gateway plugin + smart limiter
     */
    private List<K8sResourcePack> handleOtherRateLimit(ServiceMeshRateLimit rateLimit) {

        ServiceInfo serviceInfo = ServiceInfo.instance();
        serviceInfo.setServiceName(rateLimit.getHost());
        List<FragmentHolder> fragmentHolders = new ArrayList<>();
        if (!StringUtils.isEmpty(rateLimit.getPlugin())) {
            fragmentHolders = pluginService.processGlobalPlugin(Arrays.asList(rateLimit.getPlugin()), serviceInfo);
        }

        List<K8sResourcePack> resourcePacks = new ArrayList<>();

        List<TemplateParams> params = new RateLimiterDataHandler(fragmentHolders).handle(rateLimit);
        List<String> rawSmartLimiter = neverNullRenderTwiceProcessor.process(smartLimiter, params);
        List<String> rawGatewayPlugin = neverNullRenderTwiceProcessor.process(gatewayPlugin, params);

        resourcePacks.addAll(generateK8sPack(rawSmartLimiter,
                new SmartLimiterMerger(),
                new SmartLimiterSubtracter(),
                new RawSmartLimiterPreHandler(),
                new EmptyResourceGenerator(new EmptySmartLimiter(rateLimit.getServiceName(), rateLimit.getNamespace()))));
        resourcePacks.addAll(generateK8sPack(rawGatewayPlugin,
                new MeshRateLimitGatewayPluginMerger(),
                new MeshRateLimitGatewayPluginSubtracter(),
                new EmptyResourceGenerator(new EmptyGatewayPlugin(rateLimit.getHost(), rateLimit.getNamespace()))));

        return resourcePacks;
    }

    public List<K8sResourcePack> translateSidecar(String sourceApp, String sourceNamespace, String targetService) {
        List<K8sResourcePack> resources = new ArrayList<>();
        TemplateParams params = TemplateParams.instance()
                .put(TemplateConst.NAMESPACE, sourceNamespace)
                .put(TemplateConst.SIDECAR_SOURCE_APP, sourceApp)
                .put(TemplateConst.SIDECAR_EGRESS_HOSTS, Arrays.asList(targetService))
                ;
        String rawSidecar = defaultModelProcessor.process(sidecar, params);
        resources.addAll(generateK8sPack(Arrays.asList(rawSidecar)));
        return resources;
    }


    /**
     * 由于原先插件渲染domain为数组，去掉domain前面的 -
     */
    private class RawSmartLimiterPreHandler implements Function<String, String> {

        @Override
        public String apply(String s) {
            return s.replace("- domain:", "  domain:");
        }
    }

    public List<K8sResourcePack> translate(ServiceMeshCircuitBreaker circuitBreaker) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> extraCircuitBreaker;
        if (circuitBreaker.getStatus() == 1) {
            List<FragmentHolder> pluginHolders = pluginService.processPlugin(Arrays.asList(circuitBreaker.getPlugins()), new ServiceInfo());
            extraCircuitBreaker = pluginHolders
                    .stream()
                    .map(h -> h.getGatewayPluginsFragment().getContent())
                    .collect(Collectors.toList());

        }else {
            extraCircuitBreaker = new ArrayList<>();
        }
        List<String> circuitBreakers = defaultModelProcessor.process(circuitGatewayPlugin, circuitBreaker,
                new CircuitBreakerDataHandler(extraCircuitBreaker));
        String pluginName;
        if (Const.SERVICE_MESH_CIRCUIT_BREAKER_KIND.equals(circuitBreaker.getRuleType())){
            pluginName = Const.SERVICE_MESH_PLUGIN_NAME_CIRCUIT_BREAKER;
        }else {
            pluginName = Const.SERVICE_MESH_PLUGIN_NAME_DOWNGRADE;
        }
        resources.addAll(generateK8sPack(circuitBreakers,
                new CircuitConfigMapMerger(pluginName), null, r -> r, this::str2HasMetadata, hsm -> hsm));
        return resources;
    }


}
