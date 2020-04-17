package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.IstioModelEngine;
import com.netease.cloud.nsf.core.gateway.handler.VersionManagersDataHandler;
import com.netease.cloud.nsf.core.gateway.processor.DefaultModelProcessor;
import com.netease.cloud.nsf.core.gateway.processor.NeverReturnNullModelProcessor;
import com.netease.cloud.nsf.core.gateway.processor.RenderTwiceModelProcessor;
import com.netease.cloud.nsf.core.k8s.K8sResourcePack;
import com.netease.cloud.nsf.core.k8s.empty.EmptyGatewayPlugin;
import com.netease.cloud.nsf.core.k8s.empty.EmptySmartLimiter;
import com.netease.cloud.nsf.core.k8s.merger.MeshRateLimitGatewayPluginMerger;
import com.netease.cloud.nsf.core.k8s.merger.SmartLimiterMerger;
import com.netease.cloud.nsf.core.k8s.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.k8s.subtracter.MeshRateLimitGatewayPluginSubtracter;
import com.netease.cloud.nsf.core.k8s.subtracter.SmartLimiterSubtracter;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import com.netease.cloud.nsf.service.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

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
    private static final String smartLimiter = "mesh/smartLimiter";
    private static final String gatewayPlugin = "mesh/globalGatewayPlugin";

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

    /**
     * 由于原先插件渲染domain为数组，去掉domain前面的 -
     */
    private class RawSmartLimiterPreHandler implements Function<String, String> {

        @Override
        public String apply(String s) {
            return s.replace("- domain:", "  domain:");
        }
    }
}
