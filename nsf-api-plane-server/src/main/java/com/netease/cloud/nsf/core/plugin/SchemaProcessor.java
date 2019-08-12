package com.netease.cloud.nsf.core.plugin;

import com.netease.cloud.nsf.meta.ServiceInfo;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/5
 **/
public interface SchemaProcessor {
    String getName();

    String process(String plugin, ServiceInfo serviceInfo);
}
