package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.PluginTemplate;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
public interface PluginService {
    PluginTemplate getTemplate(String name, String version);

    void enablePlugin(Object serviceInfo, String plugin);
}
