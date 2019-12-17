package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.meta.Plugin;
import com.netease.cloud.nsf.meta.ServiceInfo;

import java.util.List;
import java.util.Map;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
public interface PluginService {
    Plugin getPlugin(String name);

    Map<String, Plugin> getPlugins();

    String getSchema(String path);

    String getPluginConfig();

    List<FragmentHolder> processSchema(List<String> plugins, ServiceInfo serviceInfo);

    List<String> extractService(List<String> plugins);
}
