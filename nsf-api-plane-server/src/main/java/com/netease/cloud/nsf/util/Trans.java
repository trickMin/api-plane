package com.netease.cloud.nsf.util;

import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.dto.*;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/19
 **/
public class Trans {


    public static API yxAPI2API(YxAPIDTO yxApi) {
        API api = new API();

        ApiOption option = yxApi.getOption();
        // FIXME
        BeanUtils.copyProperties(yxApi, api);
        api.setUriMatch(UriMatch.get(yxApi.getUriMatch()));
        api.setRetries(option.getRetries());
        api.setPreserveHost(option.getPreserveHost());

        return api;
    }

    public static API portalAPI2API(PortalAPIDTO portalAPI) {

        API api = new API();
        BeanUtils.copyProperties(portalAPI, api);
        api.setUriMatch(UriMatch.get(portalAPI.getUriMatch()));
        api.setProxyServices(portalAPI.getProxyServices().stream()
                                .map(ps -> portalService2Service(ps))
                                .collect(Collectors.toList()));
        api.setGateways(Arrays.asList(portalAPI.getGateway()));
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
        s.setGateway(portalService.getGateway());
        s.setProtocol(portalService.getProtocol());
        return s;
    }

    public static PluginOrder pluginOrderDTO2PluginOrder(PluginOrderDTO pluginOrderDTO) {

        PluginOrder po = new PluginOrder();
        po.setGatewayLabels(pluginOrderDTO.getGatewayLabels());
        po.setPlugins(pluginOrderDTO.getPlugins());
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
