package com.netease.cloud.nsf.core.gateway.service;

import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.ServiceHealth;

import java.util.List;

/**
 * 获取服务实例、网关实例等资源信息
 */
public interface ResourceManager {

    List<Endpoint> getEndpointList();

    List<Gateway> getGatewayList();

    List<String> getServiceList();

    Integer getServicePort(List<Endpoint> endpoints, String targetHost);

    List<ServiceHealth> getServiceHealthList(String host);
}
