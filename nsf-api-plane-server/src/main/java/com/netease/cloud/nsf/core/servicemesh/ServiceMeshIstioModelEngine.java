package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.IstioModelEngine;
import com.netease.cloud.nsf.core.gateway.handler.VersionManagersDataHandler;
import com.netease.cloud.nsf.core.gateway.processor.DefaultModelProcessor;
import com.netease.cloud.nsf.core.gateway.processor.RenderTwiceModelProcessor;
import com.netease.cloud.nsf.core.k8s.K8sResourcePack;
import com.netease.cloud.nsf.core.k8s.operator.IntegratedResourceOperator;
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
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
@Component
public class ServiceMeshIstioModelEngine extends IstioModelEngine {

    private static final Logger logger = LoggerFactory.getLogger(ServiceMeshIstioModelEngine.class);

    private DefaultModelProcessor defaultModelProcessor;
    private PluginService pluginService;
    private RenderTwiceModelProcessor renderTwiceModelProcessor;

    private static final String versionManager = "sidecarVersionManagement";
    private static final String smartLimiter = "mesh/smartLimiter";
    private static final String gatewayPlugin = "gateway/globalGatewayPlugin";

    @Autowired
    public ServiceMeshIstioModelEngine(IntegratedResourceOperator operator, TemplateTranslator templateTranslator, PluginService pluginService) {
        super(operator);
        this.pluginService = pluginService;
        this.defaultModelProcessor = new DefaultModelProcessor(templateTranslator);
        this.renderTwiceModelProcessor = new RenderTwiceModelProcessor(templateTranslator);
    }

    public List<K8sResourcePack> translate(SidecarVersionManagement svm) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> versionManagers = defaultModelProcessor.process(versionManager, svm, new VersionManagersDataHandler());
        resources.addAll(generateK8sPack(Arrays.asList(versionManagers.get(0))));
        return resources;
    }

    public List<K8sResourcePack> translate(ServiceMeshRateLimit rateLimit) {
        List<K8sResourcePack> resources = new ArrayList<>();
        //TODO translate
        ServiceInfo serviceInfo = ServiceInfo.instance();
        serviceInfo.setServiceName(rateLimit.getHost());
        List<FragmentHolder> fragmentHolders = pluginService.processGlobalPlugin(Arrays.asList(rateLimit.getPlugin()), serviceInfo);

        if (CollectionUtils.isEmpty(fragmentHolders)) {
            logger.warn("fragmentHolders is null from the plugin service");
            return resources;
        }
        List<K8sResourcePack> resourcePacks = new ArrayList<>();

        List<TemplateParams> params = new RateLimiterDataHandler(fragmentHolders.get(0)).handle(rateLimit);

        List<String> rawSmartLimiter = renderTwiceModelProcessor.process(smartLimiter, params);
        List<String> rawGatewayPlugin = renderTwiceModelProcessor.process(gatewayPlugin, params);

        resourcePacks.addAll(generateK8sPack(rawSmartLimiter));
        resourcePacks.addAll(generateK8sPack(rawGatewayPlugin));

        return resourcePacks;
    }


}
