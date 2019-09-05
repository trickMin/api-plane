package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.ConfigManager;
import com.netease.cloud.nsf.core.gateway.ConfigStore;
import com.netease.cloud.nsf.core.gateway.IstioHttpClient;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import org.springframework.beans.BeanUtils;
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
    private IstioHttpClient istioClient;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private ConfigStore configStore;

    @Value("${apiNamespace:gateway-system}")
    private String apiNamespace;

    public void updateAPI(YxAPIModel yxApi) {
        configManager.updateConfig(transform(yxApi), apiNamespace);
    }

    private API transform(YxAPIModel yxApi) {

        API api = new API();

        // FIXME
        BeanUtils.copyProperties(yxApi, api);
        return api;
    }

    @Override
    public void deleteAPI(YxAPIModel yxApi) {
        configManager.deleteConfig(transform(yxApi), apiNamespace);
    }

    @Override
    public List<IstioResource> getAPIResources(String service) {
        return configManager.getConfigResources(service, apiNamespace);
    }

    @Override
    public List<Endpoint> getServiceList() {
        return istioClient.getEndpointList();
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
