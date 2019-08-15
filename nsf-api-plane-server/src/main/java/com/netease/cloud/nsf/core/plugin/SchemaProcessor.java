package com.netease.cloud.nsf.core.plugin;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/5
 **/
public interface SchemaProcessor<T> {
    String getName();

    FragmentHolder process(String plugin, T serviceInfo);
}
