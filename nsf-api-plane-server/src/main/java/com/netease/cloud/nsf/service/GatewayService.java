package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.web.PortalService;
import me.snowdrop.istio.api.IstioResource;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public interface GatewayService {

    void updateAPI(API api);

    void deleteAPI(API api);

    void updateService(PortalService cluster);

    void deleteService(PortalService service);

    List<IstioResource> getAPIResources(String service);

    List<Endpoint> getServiceList();

    /**
     * 根据网关对应labels，获取网关后的服务
     * @param labels
     * @return
     */
    List<Endpoint> getServiceListByGateway(List<String> labels);

    List<Gateway> getGatewayList();

    List<PluginTemplate> getPluginList();

    List<GatewaySync> getGatewaySyncList();

}
