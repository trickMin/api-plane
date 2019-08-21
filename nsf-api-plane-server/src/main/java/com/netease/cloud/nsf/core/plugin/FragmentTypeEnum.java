package com.netease.cloud.nsf.core.plugin;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/20
 **/
public enum FragmentTypeEnum {
    // GatewayModelProcessor会将Fragment合并VirtualService的一个默认match中
    DEFAULT_MATCH,
    // GatewayModelProcessor会将Fragment作为新match创建到VirtualService中
    NEW_MATCH,
}
