package com.netease.cloud.nsf.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author zhangzihao
 */
@Component
public class DaemonSetConfig {

    private String defaultSidecarFilPath = "/home/envoy";

    @Autowired
    private Environment environment;


    public String getSidecarFilePath(){
        String currentPath = environment.getProperty("sidecarPath");
        if (StringUtils.isEmpty(currentPath)){
            return defaultSidecarFilPath;
        }
        return currentPath;
    }

    public String getNsfMetaUrl(){
        return environment.getProperty("nsfMetaUrl");
    }

}
