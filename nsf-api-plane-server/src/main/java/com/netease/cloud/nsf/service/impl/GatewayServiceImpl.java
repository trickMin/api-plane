package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.gateway.service.ConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.dto.*;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.Trans;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.Plugin;
import me.snowdrop.istio.api.networking.v1alpha3.PluginManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    public PluginOrderDTO getPluginOrder(PluginOrderDTO pluginOrderDto) {
        pluginOrderDto.setPlugins(new ArrayList<>());
        PluginOrderDTO dto = new PluginOrderDTO();
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginOrderDto);
        IstioResource config = configManager.getConfig(pluginOrder);
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
    public List<ServiceAndPortDTO> getServiceAndPortList(String name) {
        String pattern = ".*";
        if (!StringUtils.isEmpty(name)) {
            pattern = "^" + name + pattern + "$";
        }
        final String fPattern = pattern;
        return resourceManager.getServiceAndPortList().stream()
                .filter(sap -> Pattern.compile(fPattern).matcher(sap.getName()).find())
                .map(sap -> {
                    ServiceAndPortDTO dto = new ServiceAndPortDTO();
                    dto.setName(sap.getName());
                    dto.setPort(sap.getPort());
                    return dto;
                }).collect(Collectors.toList());
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
    public List<ServiceHealth> getServiceHealthList(String host) {
        return resourceManager.getServiceHealthList(host);
    }


}
