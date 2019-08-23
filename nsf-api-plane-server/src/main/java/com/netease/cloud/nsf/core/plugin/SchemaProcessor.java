package com.netease.cloud.nsf.core.plugin;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/5
 **/
public interface SchemaProcessor<T> {
    // processor名，对应label #@processor
    String getName();

    FragmentHolder process(String plugin, T serviceInfo);
}
