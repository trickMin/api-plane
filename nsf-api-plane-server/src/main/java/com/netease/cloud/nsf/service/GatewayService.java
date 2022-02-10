package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.dto.*;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public interface GatewayService {

    void updateAPI(PortalAPIDTO portalAPI);

    void deleteAPI(PortalAPIDeleteDTO portalAPI);

    void updateGatewayPlugin(GatewayPluginDTO plugin);

    void deleteGatewayPlugin(GatewayPluginDTO plugin);

    void updateService(PortalServiceDTO service);

    /**
     * 用于服务发布时参数校验
     *
     * @param service
     * @return
     */
    ErrorCode checkUpdateService(PortalServiceDTO service);

    void deleteService(PortalServiceDTO service);

    PluginOrderDTO getPluginOrder(PluginOrderDTO pluginOrderDto);

    void updatePluginOrder(PluginOrderDTO pluginOrderDto);

    void deletePluginOrder(PluginOrderDTO pluginOrderDTO);

    List<String> getServiceList();

    List<ServiceAndPortDTO> getServiceAndPortList(String name, String type, String registryId);

    List<Gateway> getGatewayList();

    List<ServiceHealth> getServiceHealthList(String host, List<String> subsets, String gateway);

    void updateIstioGateway(PortalIstioGatewayDTO portalGateway);

    PortalIstioGatewayDTO getIstioGateway(String clusterName);

    /**
     * 获取Dubbo Meta元数据信息
     *
     * @param igv             接口+版本+分组 {interface:group:version}
     * @param applicationName 应用名称
     * @param method          dubbo方法
     * @return
     */
    List<DubboMetaDto> getDubboMeta(String igv, String applicationName, String method);

}
