package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.service.ConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ConfigStore;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.dto.PluginOrderDTO;
import com.netease.cloud.nsf.meta.dto.PortalAPIDTO;
import com.netease.cloud.nsf.meta.dto.PortalServiceDTO;
import com.netease.cloud.nsf.meta.dto.YxAPIDTO;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.Trans;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private ConfigStore configStore;

    @Value("${apiNamespace:gateway-system}")
    private String apiNamespace;

    @Override
    public void updateAPI(YxAPIDTO api) {
        configManager.updateConfig(Trans.yxAPI2API(api), apiNamespace);
    }

    @Override
    public void deleteAPI(YxAPIDTO api) {
        configManager.deleteConfig(Trans.yxAPI2API(api), apiNamespace);
    }

    @Override
    public void updateAPI(PortalAPIDTO api) {
        configManager.updateConfig(Trans.portalAPI2API(api), apiNamespace);
    }

    @Override
    public void deleteAPI(PortalAPIDTO api) {
        configManager.deleteConfig(Trans.portalAPI2API(api), apiNamespace);
    }


    @Override
    public void updateService(PortalServiceDTO service) {
        configManager.updateConfig(Trans.portalService2Service(service), apiNamespace);
    }

    @Override
    public void deleteService(PortalServiceDTO service) {
        configManager.deleteConfig(Trans.portalService2Service(service), apiNamespace);
    }

    @Override
    public void updatePluginOrder(PluginOrderDTO pluginOrderDto) {
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginOrderDto);
        configManager.updateConfig(pluginOrder, apiNamespace);
    }

    @Override
    public void deletePluginOrder(PluginOrderDTO pluginOrderDTO) {
        PluginOrder pluginOrder = Trans.pluginOrderDTO2PluginOrder(pluginOrderDTO);
        configManager.deleteConfig(pluginOrder, apiNamespace);
    }

    @Override
    public List<Endpoint> getServiceList() {
        return resourceManager.getEndpointList();
    }

    @Override
    public List<Endpoint> getServiceListByGateway(List<String> labels) {

        Map<String, String> labelMap = CommonUtil.strs2Label(labels);
        for (String rawLabel : labels) {
            if (!rawLabel.contains(COLON)) continue;
            String[] split = rawLabel.split(COLON);
            labelMap.put(split[0].trim(), split[1].trim());
        }
        if (labelMap.isEmpty()) return Collections.emptyList();

        //1. 根据标签找到对应的gateway，然后得到gateway名
        List<IstioResource> gateways = configStore.get(K8sResourceEnum.Gateway.name(), apiNamespace);
        if (CollectionUtils.isEmpty(gateways)) return Collections.emptyList();
        List<String> gatewayNames = gateways.stream()
                .filter(g -> {
                    me.snowdrop.istio.api.networking.v1alpha3.Gateway gateway = (me.snowdrop.istio.api.networking.v1alpha3.Gateway) g;
                    if (gateway.getSpec() == null || gateway.getSpec().getSelector() == null) return false;
                    Map<String, String> selectors = gateway.getSpec().getSelector();
                    for (Map.Entry<String, String> l : labelMap.entrySet()) {
                        if (!selectors.containsKey(l.getKey()) ||
                                !selectors.get(l.getKey()).equals(l.getValue())) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(g -> g.getMetadata().getName())
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(gatewayNames)) return Collections.emptyList();

        //2. 得到关联了该gateway的virtualservice，并取出destination中的host
        List<IstioResource> virtualServices = configStore.get(K8sResourceEnum.VirtualService.name(), apiNamespace);
        if (CollectionUtils.isEmpty(virtualServices)) return Collections.emptyList();
        List<String> vsHosts = virtualServices.stream()
                .filter(i -> {
                    VirtualService vs = (VirtualService) i;
                    if (vs.getSpec() == null ||
                            vs.getSpec().getGateways() == null) return false;
                    return !Collections.disjoint(vs.getSpec().getGateways(), gatewayNames);
                })
                .map(i -> {
                    ResourceGenerator gen = K8sResourceGenerator.newInstance(i, ResourceType.OBJECT);
                    List<String> hosts = gen.getValue("$..destination.host");
                    return hosts;
                })
                .flatMap(h -> h.stream())
                .distinct()
                .collect(Collectors.toList());

        //3. 查找所有destination rule，与之前得到的host做对比，取有交集的部分
        List<IstioResource> destinationrules = configStore.get(K8sResourceEnum.DestinationRule.name(), apiNamespace);
        if (CollectionUtils.isEmpty(destinationrules)) return Collections.emptyList();
        List<String> dsHosts = destinationrules.stream()
                .map(i -> {
                    ResourceGenerator gen = K8sResourceGenerator.newInstance(i, ResourceType.OBJECT);
                    String host = gen.getValue("$.spec.host");
                    return host;
                })
                .distinct()
                .collect(Collectors.toList());

        dsHosts.retainAll(vsHosts);

        return dsHosts.stream()
                .map(h -> {
                    Endpoint e = new Endpoint();
                    e.setHostname(h);
                    return e; })
                .collect(Collectors.toList());
    }

    @Override
    public List<Gateway> getGatewayList() {
        return resourceManager.getGatewayList();
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
