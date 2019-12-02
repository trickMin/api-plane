package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.gateway.service.ConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.PluginOrder;
import com.netease.cloud.nsf.meta.ServiceHealth;
import com.netease.cloud.nsf.meta.dto.PluginOrderDTO;
import com.netease.cloud.nsf.meta.dto.PortalAPIDTO;
import com.netease.cloud.nsf.meta.dto.PortalServiceDTO;
import com.netease.cloud.nsf.meta.dto.YxAPIDTO;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.Trans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Service
public class GatewayServiceImpl implements GatewayService {

    private static final String COLON = ":";

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private ConfigManager configManager;

    @Override
    public void updateAPI(YxAPIDTO api) {
        configManager.updateConfig(Trans.yxAPI2API(api));
    }

    @Override
    public void deleteAPI(YxAPIDTO api) {
        configManager.deleteConfig(Trans.yxAPI2API(api));
    }

    @Override
    public void updateAPI(PortalAPIDTO api) {
        configManager.updateConfig(Trans.portalAPI2API(api));
    }

    @Override
    public void deleteAPI(PortalAPIDTO api) {
        configManager.deleteConfig(Trans.portalAPI2API(api));
    }


    @Override
    public void updateService(PortalServiceDTO service) {
        configManager.updateConfig(Trans.portalService2Service(service));
    }

    @Override
    public void deleteService(PortalServiceDTO service) {
        configManager.deleteConfig(Trans.portalService2Service(service));
    }

    @Override
    public void updatePluginOrder(PluginOrderDTO pluginOrderDto) {
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginOrderDto);
        configManager.updateConfig(pluginOrder);
    }

    @Override
    public void deletePluginOrder(PluginOrderDTO pluginOrderDTO) {
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginOrderDTO);
        configManager.deleteConfig(pluginOrder);
    }

    @Override
    public List<String> getServiceList() {
        return resourceManager.getServiceList();
    }

    @Override
    public List<Gateway> getGatewayList() {
        return resourceManager.getGatewayList();
    }

    @Override
    public void updateSVM(SidecarVersionManagement svm) {
        configManager.updateConfig(svm);
    }

    @Override
    public List<PodStatus> queryByPodNameList(PodVersion podVersion) {
        return configManager.querySVMConfig(podVersion);
    }

    @Override
    public List<ServiceHealth> getServiceHealthList() {
        return resourceManager.getServiceHealthList();
    }


}
