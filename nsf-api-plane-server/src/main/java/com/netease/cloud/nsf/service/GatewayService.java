package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.*;
import me.snowdrop.istio.api.IstioResource;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public interface GatewayService {

    void updateAPI(YxAPIModel api);

    void deleteAPI(String service, String name);

    List<IstioResource> getAPIResources(String service);

    List<String> getServiceList();

    List<Gateway> getGatewayList();

    List<PluginTemplate> getPluginList();

    List<GatewaySync> getGatewaySyncList();
}
