package com.netease.cloud.nsf.configuration;

import com.netease.cloud.nsf.cache.K8sResourceCache;
import com.netease.cloud.nsf.cache.listener.InformerListenerManager;
import com.netease.cloud.nsf.mixer.MixerApa;
import com.netease.cloud.nsf.util.Const;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * 暂时只隔离网格场景下的Bean, 即网格模式下有所有Bean, 网关模式下不含网格的Bean
 * 暂不考虑网格的容器、非容器配置隔离
 *
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/6/15
 **/
@Profile("sm")
@Configuration
@AutoConfigureBefore(ApiPlaneAutoBaseConfiguration.class)
public class MeshAutoConfiguration extends ApiPlaneAutoBaseConfiguration {

    @PostConstruct
    public void init() {
        System.out.println("mesh auto config");
    }

    @Bean
    ApiPlaneConfig apiPlaneConfig(Environment environment){
        ApiPlaneConfig apiPlaneConfig = new ApiPlaneConfig();
        apiPlaneConfig.setNsfMetaUrl(environment.getProperty("nsfMetaUrl"));
        if (!StringUtils.isEmpty(environment.getProperty("startInformer"))){
            apiPlaneConfig.setStartInformer(environment.getProperty("startInformer"));
        }
        if (StringUtils.isEmpty(environment.getProperty("daemonSetName"))){
            apiPlaneConfig.setDaemonSetName(Const.DOWNLOAD_DAEMONSET_NAME);
        }else {
            apiPlaneConfig.setDaemonSetName(environment.getProperty("daemonSetName"));
        }
        if (StringUtils.isEmpty(environment.getProperty("daemonSetNamespace"))){
            apiPlaneConfig.setDaemonSetNamespace(Const.DOWNLOAD_DAEMONSET_NAMESPACE);
        }else {
            apiPlaneConfig.setDaemonSetNamespace(environment.getProperty("daemonSetNamespace"));
        }
        if (StringUtils.isEmpty(environment.getProperty("daemonSetPort"))){
            apiPlaneConfig.setDaemonSetPort(Const.DOWNLOAD_DAEMONSET_PORT);
        }else {
            apiPlaneConfig.setDaemonSetPort(environment.getProperty("daemonSetPort"));
        }
        return apiPlaneConfig;
    }

    @Bean
    K8sResourceCache k8sResourceCache() {
        return new K8sResourceCache<>();
    }

    @Bean
    MixerApa mixerApa() {
        return new MixerApa();
    }

    @Bean
    InformerListenerManager informerListenerManager() {
        return new InformerListenerManager();
    }


}


