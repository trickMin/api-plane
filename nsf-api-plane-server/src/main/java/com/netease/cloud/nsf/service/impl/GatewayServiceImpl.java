package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.gateway.ConfigManager;
import com.netease.cloud.nsf.core.gateway.IstioClient;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Service
public class GatewayServiceImpl implements GatewayService {

    @Autowired
    private IstioClient istioClient;

    @Autowired
    private ConfigManager manager;

    public void updateAPI(APIModel api) {
        manager.updateConfig(api);
    }

    @Override
    public void deleteAPI(String service, String name) {
        manager.deleteConfig(service, name);
    }

    @Override
    public List<IstioResource> getAPIResources(String service) {
        return manager.getConfigResources(service);
    }

    @Override
    public List<String> getServiceList() {
        return istioClient.getServiceNameList();
    }

    @Override
    public List<Gateway> getGatewayList() {
        return null;
    }

    @Override
    public List<PluginTemplate> getPluginList() {
        return null;
    }

    @Override
    public List<GatewaySync> getGatewaySyncList() {
        return null;
    }
}
