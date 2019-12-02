package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Gateway;
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

    PluginOrderDTO getPluginOrder();

    void updatePluginOrder(PluginOrderDTO pluginOrderDto);

    void deletePluginOrder(PluginOrderDTO pluginOrderDTO);

    List<String> getServiceList();

    List<Gateway> getGatewayList();

}
