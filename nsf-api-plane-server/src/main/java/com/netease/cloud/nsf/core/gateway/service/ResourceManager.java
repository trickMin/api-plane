package com.netease.cloud.nsf.core.gateway.service;

import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Gateway;

import java.util.List;

/**
 * 获取服务实例、网关实例等资源信息
 */
public interface ResourceManager {

    List<Endpoint> getEndpointList();

    List<Gateway> getGatewayList();

    List<String> getServiceList();
}
