package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.GatewaySync;
import com.netease.cloud.nsf.meta.Plugin;
import com.netease.cloud.nsf.meta.dto.PluginOrderDTO;
import com.netease.cloud.nsf.meta.dto.PortalAPIDTO;
import com.netease.cloud.nsf.meta.dto.PortalServiceDTO;
import com.netease.cloud.nsf.meta.dto.YxAPIDTO;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public interface GatewayService {

    void updateAPI(YxAPIDTO api);

    void deleteAPI(YxAPIDTO api);

    void updateAPI(PortalAPIDTO portalAPI);

    void deleteAPI(PortalAPIDTO portalAPI);

    void updateService(PortalServiceDTO service);

    void deleteService(PortalServiceDTO service);

    void updatePluginOrder(PluginOrderDTO pluginOrderDto);

    void deletePluginOrder(PluginOrderDTO pluginOrderDTO);

    List<Endpoint> getServiceList();

    /**
     * 根据网关对应labels，获取网关后的服务
     * @param labels
     * @return
     */
    List<Endpoint> getServiceListByGateway(List<String> labels);

    List<Gateway> getGatewayList();

    List<GatewaySync> getGatewaySyncList();

}
