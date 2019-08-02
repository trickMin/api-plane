package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.meta.PluginTemplate;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
public interface PluginService {
    PluginTemplate getTemplate(String name, String version);

    String processTemplate(String name, String version, Object model, ResourceType modelType);
}
