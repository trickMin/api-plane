package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.gateway.service.ConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.PluginOrder;
import com.netease.cloud.nsf.meta.dto.*;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.util.Trans;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.Plugin;
import me.snowdrop.istio.api.networking.v1alpha3.PluginManager;
import me.snowdrop.istio.api.networking.v1alpha3.VersionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public PluginOrderDTO getPluginOrder() {
        PluginOrderDTO dto = new PluginOrderDTO();
        IstioResource config = configManager.getConfig(new PluginOrder());
        if (Objects.isNull(config)) throw new ApiPlaneException("plugin manager config can not found.");
        PluginManager pm = (PluginManager) config;
        dto.setGatewayLabels(pm.getSpec().getWorkloadLabels());
        List<Plugin> plugins = pm.getSpec().getPlugin();
        dto.setPlugins(new ArrayList<>());
        if (CollectionUtils.isEmpty(plugins)) return dto;
        plugins.forEach(p -> {
            PluginOrderItemDTO itemDTO = new PluginOrderItemDTO();
            itemDTO.setEnable(p.getEnable());
            itemDTO.setName(p.getName());
            itemDTO.setSettings(p.getSettings());
            dto.getPlugins().add(itemDTO);
        });
        return dto;
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

}
