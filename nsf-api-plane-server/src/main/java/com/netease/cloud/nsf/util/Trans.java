package com.netease.cloud.nsf.util;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.dto.*;
import me.snowdrop.istio.api.networking.v1alpha3.Int64Range;
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

        if (!CollectionUtils.isEmpty(api.getGateways())) {
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

        return api;
    }

    public static API portalAPI2API(PortalAPIDTO portalAPI) {

        API api = new API();
        BeanUtils.copyProperties(portalAPI, api);
        api.setUriMatch(UriMatch.get(portalAPI.getUriMatch()));
        api.setProxyServices(portalAPI.getProxyServices().stream()
                .map(ps -> portalService2Service(ps))
                .collect(Collectors.toList()));
        api.setGateways(Arrays.asList(portalAPI.getGateway().toLowerCase()));
        api.setName(portalAPI.getCode());

        api.setHeaders(pairsDTO2Pairs(portalAPI.getHeaders()));
        api.setQueryParams(pairsDTO2Pairs(portalAPI.getQueryParams()));
        api.setPriority(portalAPI.getPriority());
        return api;
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
        return s;
    }

    private static List<Int64Range> int2Range(List<Integer> expectedStatuses) {
        if (CollectionUtils.isEmpty(expectedStatuses)) return Collections.emptyList();

        return expectedStatuses.stream()
                    .map(s -> new Int64Range(s, s))
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
