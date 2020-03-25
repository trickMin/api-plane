package com.netease.cloud.nsf.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/3/19
 **/
@Configuration
public class GlobalConfig {

    @Value("${resourceNamespace:gateway-system}")
    private String resourceNamespace;

    @Value("${apiPlaneType}")
    private String apiPlaneType;

    @Value("${apiPlaneVersion}")
    private String apiPlaneVersion;

    public String getResourceNamespace() {
        return resourceNamespace;
    }

    public String getApiPlaneType() {
        return apiPlaneType;
    }

    public String getApiPlaneVersion() {
        return apiPlaneVersion;
    }
}
