package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.*;
import me.snowdrop.istio.api.IstioResource;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public interface GatewayService {

    void updateAPI(YxAPIModel api);

    void deleteAPI(YxAPIModel api);

    List<IstioResource> getAPIResources(String service);

    List<Endpoint> getServiceList();

    /**
     * 根据网关对应labels，获取网关后的服务
     * @param label
     * @return
     */
    List<Endpoint> getServiceList(String label);

    List<Gateway> getGatewayList();

    List<PluginTemplate> getPluginList();

    List<GatewaySync> getGatewaySyncList();

}
