package com.netease.cloud.nsf.configuration.env;

import com.netease.cloud.nsf.configuration.GatewayAutoConfiguration;
import com.netease.cloud.nsf.core.GlobalConfig;
import com.netease.cloud.nsf.core.gateway.GatewayIstioModelEngine;
import com.netease.cloud.nsf.core.gateway.service.GatewayConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.core.gateway.service.impl.GatewayConfigManagerImpl;
import com.netease.cloud.nsf.core.gateway.service.impl.K8sConfigStore;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.service.impl.GatewayServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/26
 **/
@Configuration
@ConditionalOnMissingBean(GatewayNonK8sConfiguration.class)
public class GatewayK8sConfiguration extends GatewayAutoConfiguration {

    @Bean
    @Primary
    public K8sConfigStore configStore(KubernetesClient client, GlobalConfig globalConfig) {
        return new K8sConfigStore(client, globalConfig);
    }

    @Bean
    public GatewayConfigManager gatewayConfigManager(GatewayIstioModelEngine modelEngine, K8sConfigStore k8sConfigStore, GlobalConfig globalConfig) {
        return new GatewayConfigManagerImpl(modelEngine, k8sConfigStore, globalConfig);
    }

    @Bean
    public GatewayService gatewayService(ResourceManager resourceManager, GatewayConfigManager configManager) {
        return new GatewayServiceImpl(resourceManager, configManager);
    }
}
