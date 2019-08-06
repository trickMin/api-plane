package com.netease.cloud.nsf.core.plugin;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/5
 **/
public interface SchemaProcessor {
    String getName();

    String process(String plugin);
}
