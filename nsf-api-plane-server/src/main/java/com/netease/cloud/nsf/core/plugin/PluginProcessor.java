package com.netease.cloud.nsf.core.plugin;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
public interface PluginProcessor {
    void process(Object serviceInfo, String plugin, String schema);
}
