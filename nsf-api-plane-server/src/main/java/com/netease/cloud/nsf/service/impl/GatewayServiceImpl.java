package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.gateway.service.ConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.dto.*;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.Trans;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.GatewaySpec;
import me.snowdrop.istio.api.networking.v1alpha3.Plugin;
import me.snowdrop.istio.api.networking.v1alpha3.PluginManager;
import me.snowdrop.istio.api.networking.v1alpha3.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public void deleteAPI(PortalAPIDeleteDTO api) {
        configManager.deleteConfig(Trans.portalDeleteAPI2API(api));
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
    public List<ServiceAndPortDTO> getServiceAndPortList(String name, String type, String registryId) {
        String pattern = ".*";
        if (!StringUtils.isEmpty(name)) {
            pattern = "^" + name + pattern + "$";
        }
        final String fPattern = pattern;
        return resourceManager.getServiceAndPortList().stream()
                .filter(sap -> Pattern.compile(fPattern).matcher(sap.getName()).find())
                .filter(sap -> matchType(type, sap.getName(), registryId))
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

    private boolean matchType(String type, String name, String registryId) {
        if (StringUtils.isEmpty(type)) return true;
        if (type.equalsIgnoreCase(Const.SERVICE_TYPE_CONSUL) && StringUtils.isEmpty(registryId) && Pattern.compile(".*\\.consul\\.(.*?)").matcher(name).find()) return true;
        if (type.equalsIgnoreCase(Const.SERVICE_TYPE_CONSUL) && name.endsWith(String.format(".consul.%s", registryId))) return true;
        if (type.equalsIgnoreCase(Const.SERVICE_TYPE_K8S) && name.endsWith(".svc.cluster.local")) return true;
        return false;
    }
    @Override
    public void updateIstioGateway(PortalIstioGatewayDTO portalGateway) {
        configManager.updateConfig(Trans.portalGW2GW(portalGateway));
    }

    @Override
    public PortalIstioGatewayDTO getIstioGateway(String clusterName) {
        IstioGateway istioGateway = new IstioGateway();
        istioGateway.setGwCluster(clusterName);
        IstioResource config = configManager.getConfig(istioGateway);
        if (config == null) {
            return null;
        }

        GatewaySpec spec = (GatewaySpec) config.getSpec();
        final String gwCluster = "gw_cluster";
        Map<String, String> selector = spec.getSelector();
        if (CollectionUtils.isEmpty(selector)){
            selector.get(gwCluster);
        }
        istioGateway.setName(config.getMetadata().getName());
        if (CollectionUtils.isEmpty(spec.getServers()) || spec.getServers().get(0) == null) {
            return null;
        }
        Server server = spec.getServers().get(0);
        istioGateway.setXffNumTrustedHops(server.getXffNumTrustedHops());
        istioGateway.setCustomIpAddressHeader(server.getCustomIpAddressHeader());
        istioGateway.setUseRemoteAddress(server.getUseRemoteAddress() == null ? null : String.valueOf(server.getUseRemoteAddress()));
        return Trans.GW2portal(istioGateway);
    }
}
