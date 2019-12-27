package com.netease.cloud.nsf.util;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.dto.*;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/19
 **/
public class Trans {


    public static API yxAPI2API(YxAPIDTO yxApi) {
        API api = new API();

        if (!CollectionUtils.isEmpty(yxApi.getGateways())) {
            api.setGateways(yxApi.getGateways().stream()
                        .map(g -> g.toLowerCase())
                        .collect(Collectors.toList()));
        }
        ApiOption option = yxApi.getOption();
        api.setUriMatch(UriMatch.get(yxApi.getUriMatch()));
        api.setRetries(option.getRetries());
        api.setPreserveHost(option.getPreserveHost());
        api.setHosts(yxApi.getHosts());
        api.setMethods(yxApi.getMethods());
        api.setName(yxApi.getName());
        api.setPlugins(yxApi.getPlugins());
        api.setProxyUris(yxApi.getProxyUris());
        api.setRequestUris(yxApi.getRequestUris());
        api.setService(yxApi.getService());
        api.setHeaders(pairsDTO2Pairs(yxApi.getHeaders()));
        api.setQueryParams(pairsDTO2Pairs(yxApi.getQueryParams()));

        return api;
    }

    public static API portalAPI2API(PortalAPIDTO portalAPI) {

        API api = new API();
        BeanUtils.copyProperties(portalAPI, api);
        api.setUriMatch(UriMatch.get(portalAPI.getUriMatch()));
        api.setProxyServices(portalAPI.getProxyServices().stream()
                .map(ps -> portalRouteService2Service(ps))
                .collect(Collectors.toList()));
        api.setGateways(Arrays.asList(portalAPI.getGateway().toLowerCase()));
        api.setName(portalAPI.getCode());

        api.setHeaders(pairsDTO2Pairs(portalAPI.getHeaders()));
        api.setQueryParams(pairsDTO2Pairs(portalAPI.getQueryParams()));
        api.setPriority(portalAPI.getPriority());
        api.setServiceTag(portalAPI.getServiceTag());
        api.setApiId(portalAPI.getRouteId());
        return api;
    }

    public static Service portalRouteService2Service(PortalRouteServiceDTO portalRouteService) {
        Service s = new Service();
        s.setCode(portalRouteService.getCode().toLowerCase());
        s.setType(portalRouteService.getType());
        s.setWeight(portalRouteService.getWeight());
        s.setBackendService(portalRouteService.getBackendService());

        Integer port = portalRouteService.getPort();
        if (portalRouteService.getType().equals(Const.PROXY_SERVICE_TYPE_DYNAMIC) && port == null) {
            throw new ApiPlaneException("dynamic service must have port " + s.getCode());
        }
        s.setPort(port);
        s.setSubset(portalRouteService.getSubset());
        return s;
    }

    public static Service portalService2Service(PortalServiceDTO portalService) {

        Service s = new Service();
        s.setCode(portalService.getCode().toLowerCase());
        s.setType(portalService.getType());
        s.setWeight(portalService.getWeight());
        s.setBackendService(portalService.getBackendService());
        if (!StringUtils.isEmpty(portalService.getGateway())) {
            s.setGateway(portalService.getGateway().toLowerCase());
        }
        s.setProtocol(portalService.getProtocol());
        s.setConsecutiveErrors(portalService.getConsecutiveErrors());
        s.setBaseEjectionTime(portalService.getBaseEjectionTime());
        s.setMaxEjectionPercent(portalService.getMaxEjectionPercent());
        s.setServiceTag(portalService.getServiceTag());
        if (portalService.getHealthCheck() != null) {
            HealthCheckDTO healthCheck = portalService.getHealthCheck();
            s.setPath(healthCheck.getPath());
            s.setTimeout(healthCheck.getTimeout());
            s.setExpectedStatuses(healthCheck.getExpectedStatuses());
            s.setHealthyInterval(healthCheck.getHealthyInterval());
            s.setHealthyThreshold(healthCheck.getHealthyThreshold());
            s.setUnhealthyInterval(healthCheck.getUnhealthyInterval());
            s.setUnhealthyThreshold(healthCheck.getUnhealthyThreshold());
        }
        s.setLoadBalancer(portalService.getLoadBalancer());
        s.setSubsets(subsetDTO2Subset(portalService.getSubsets()));
        return s;
    }

    private static List<ServiceSubset> subsetDTO2Subset(List<ServiceSubsetDTO> subsets) {
        if (CollectionUtils.isEmpty(subsets)) return Collections.emptyList();
        return subsets.stream()
                    .map(sd -> {
                        ServiceSubset ss = new ServiceSubset();
                        ss.setLabels(sd.getLabels());
                        ss.setName(sd.getName());
                        return ss;
                    })
                    .collect(Collectors.toList());
    }

    public static PluginOrder pluginOrderDTO2PluginOrder(PluginOrderDTO pluginOrderDTO) {

        PluginOrder po = new PluginOrder();
        po.setGatewayLabels(pluginOrderDTO.getGatewayLabels());
        List<String> orderItems = new ArrayList<>();
        for (PluginOrderItemDTO dto : pluginOrderDTO.getPlugins()) {
            if (Objects.nonNull(dto)) {
                orderItems.add(ResourceGenerator.newInstance(dto, ResourceType.OBJECT).yamlString());
            }
        }
        po.setPlugins(orderItems);
        return po;
    }

    private static List<PairMatch> pairsDTO2Pairs(List<PairMatchDTO> pairMatchDTOS) {
        if (CollectionUtils.isEmpty(pairMatchDTOS)) return Collections.emptyList();
        return pairMatchDTOS.stream()
                .map(dto -> pairDTO2Pair(dto))
                .collect(Collectors.toList());
    }

    private static PairMatch pairDTO2Pair(PairMatchDTO pairMatchDTO) {
        PairMatch pm = new PairMatch();
        if (pairMatchDTO == null) return pm;
        pm.setType(pairMatchDTO.getType());
        pm.setKey(pairMatchDTO.getKey());
        pm.setValue(pairMatchDTO.getValue());
        return pm;
    }
}
