package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.gateway.ConfigManager;
import com.netease.cloud.nsf.core.gateway.IstioHttpClient;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.service.GatewayService;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Service
public class GatewayServiceImpl implements GatewayService {

    @Autowired
    private IstioHttpClient istioClient;

    @Autowired
    private ConfigManager configManager;

    public void updateAPI(YxAPIModel yxApi) {
        configManager.updateConfig(transform(yxApi));
    }

    private API transform(YxAPIModel yxApi) {

        API api = new API();
        // FIXME
        BeanUtils.copyProperties(yxApi, api);
        return api;
    }

    @Override
    public void deleteAPI(YxAPIModel yxApi) {
        configManager.deleteConfig(transform(yxApi));
    }

    @Override
    public List<IstioResource> getAPIResources(String service) {
        return configManager.getConfigResources(service);
    }

    @Override
    public List<Endpoint> getServiceList() {
        return istioClient.getEndpointList();
    }

    @Override
    public List<Gateway> getGatewayList() {
        return istioClient.getGatewayList();
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
