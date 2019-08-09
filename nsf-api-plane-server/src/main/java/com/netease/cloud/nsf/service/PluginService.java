package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.PluginTemplate;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
public interface PluginService {
    PluginTemplate getTemplate(String name, String version);

    String processSchema(String plugin);

    List<String> extractService(List<String> plugins);
}
