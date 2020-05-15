package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.IstioModelEngine;
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
import com.netease.cloud.nsf.core.servicemesh.handler.RateLimiterConfigMapDataHandler;
import com.netease.cloud.nsf.core.servicemesh.handler.RateLimiterGatewayPluginDataHandler;
import com.netease.cloud.nsf.core.servicemesh.handler.RateLimiterSmartLimiterDataHandler;
import com.netease.cloud.nsf.core.template.TemplateConst;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.meta.ServiceMeshCircuitBreaker;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import com.netease.cloud.nsf.service.PluginService;
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

    @Value(value = "${meshRateLimitServerNamespace:gateway-system}")
    String meshRateLimitServerNamespace;


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

    /**
     * 限流分为全局和单机，
     * 全局需要创建gateayplugin + configmap
     * 单机需要创建gatewayplugin + smartlimiter
     * @param rateLimit
     * @return
     */
    public List<K8sResourcePack> translate(ServiceMeshRateLimit rateLimit) {

        ServiceInfo serviceInfo = ServiceInfo.instance();
        serviceInfo.setServiceName(rateLimit.getHost());
        List<FragmentHolder> fragmentHolders = new ArrayList<>();
        if (!StringUtils.isEmpty(rateLimit.getPlugin())) {
            fragmentHolders = pluginService.processGlobalPlugin(Arrays.asList(rateLimit.getPlugin()), serviceInfo);
        }

        List<K8sResourcePack> resourcePacks = new ArrayList<>();

        FragmentHolder firstFragmentHodler = CollectionUtils.isEmpty(fragmentHolders) ? new FragmentHolder() : fragmentHolders.get(0);
        List<String> rawSmartLimiter = neverNullRenderTwiceProcessor.process(smartLimiter, rateLimit,
                new RateLimiterSmartLimiterDataHandler(firstFragmentHodler.getSmartLimiterFragment()));
        List<String> rawGatewayPlugin = neverNullRenderTwiceProcessor.process(gatewayPlugin, rateLimit,
                new RateLimiterGatewayPluginDataHandler(firstFragmentHodler.getGatewayPluginsFragment()));
        List<String> rawConfigMap = neverNullRenderTwiceProcessor.process(rlsConfigMap, rateLimit,
                new RateLimiterConfigMapDataHandler(firstFragmentHodler.getSharedConfigFragment(), rateLimitConfigMapName, meshRateLimitServerNamespace));

        resourcePacks.addAll(generateK8sPack(rawSmartLimiter,
                new SmartLimiterMerger(),
                new SmartLimiterSubtracter(),
                new EmptyResourceGenerator(new EmptySmartLimiter(rateLimit.getServiceName(), rateLimit.getNamespace()))));
        resourcePacks.addAll(generateK8sPack(rawGatewayPlugin,
                new MeshRateLimitGatewayPluginMerger(),
                new MeshRateLimitGatewayPluginSubtracter(),
                new EmptyResourceGenerator(new EmptyGatewayPlugin(rateLimit.getHost(), rateLimit.getNamespace()))));
        resourcePacks.addAll(generateK8sPack(rawConfigMap,
                new MeshRateLimitConfigMapMerger(),
                new MeshRateLimitConfigMapSubtracter(rateLimit.getHost()),
                new EmptyResourceGenerator(new EmptyConfigMap(rateLimitConfigMapName))));

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
