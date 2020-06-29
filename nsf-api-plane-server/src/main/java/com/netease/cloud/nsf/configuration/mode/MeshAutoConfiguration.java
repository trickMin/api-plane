package com.netease.cloud.nsf.configuration.mode;

import com.netease.cloud.nsf.cache.K8sResourceCache;
import com.netease.cloud.nsf.cache.listener.InformerListenerManager;
import com.netease.cloud.nsf.configuration.ApiPlaneAutoBaseConfiguration;
import com.netease.cloud.nsf.configuration.ext.ApiPlaneConfig;
import com.netease.cloud.nsf.mixer.MixerApa;
import com.netease.cloud.nsf.util.Const;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * 网格模式下的configuration
 *
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/6/15
 **/
@Profile("sm")
@Configuration
@AutoConfigureBefore(ApiPlaneAutoBaseConfiguration.class)
public class MeshAutoConfiguration {

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

//    @Bean
    MixerApa mixerApa() {
        return new MixerApa();
    }

    @Bean
    InformerListenerManager informerListenerManager() {
        return new InformerListenerManager();
    }


}


