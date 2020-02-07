package com.netease.cloud.nsf.util;

import com.google.common.collect.ImmutableList;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.dto.*;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.apache.commons.lang3.BooleanUtils;
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
        api.setApiName(portalAPI.getRouteName());
        return api;
    }

    public static IstioGateway portalGW2GW(PortalIstioGatewayDTO portalGateway) {
        if (portalGateway == null) {
            return null;
        }
        IstioGateway istioGateway = new IstioGateway();
        istioGateway.setName(portalGateway.getName());
        istioGateway.setGwCluster(portalGateway.getGwCluster());
        istioGateway.setCustomIpAddressHeader(portalGateway.getCustomIpAddressHeader());
        istioGateway.setUseRemoteAddress(portalGateway.getUseRemoteAddress() == null ? null : String.valueOf(portalGateway.getUseRemoteAddress()));
        istioGateway.setXffNumTrustedHops(portalGateway.getXffNumTrustedHops() == null ? null : (portalGateway.getXffNumTrustedHops() - 1));
        return istioGateway;
    }

    public static PortalIstioGatewayDTO GW2portal(IstioGateway istioGateway) {
        if (istioGateway == null) {
            return null;
        }
        PortalIstioGatewayDTO portalIstioGatewayDTO = new PortalIstioGatewayDTO();
        portalIstioGatewayDTO.setName(istioGateway.getName());
        portalIstioGatewayDTO.setGwCluster(istioGateway.getGwCluster());
        portalIstioGatewayDTO.setCustomIpAddressHeader(istioGateway.getCustomIpAddressHeader());
        portalIstioGatewayDTO.setUseRemoteAddress(BooleanUtils.toBooleanObject(istioGateway.getUseRemoteAddress()));
        portalIstioGatewayDTO.setXffNumTrustedHops(istioGateway.getXffNumTrustedHops() == null ? 1 : (istioGateway.getXffNumTrustedHops() + 1));

        return portalIstioGatewayDTO;
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
        if (portalService.getTrafficPolicy() != null) {
            PortalTrafficPolicyDTO trafficPolicy = portalService.getTrafficPolicy();
            PortalOutlierDetectionDTO outlierDetection = trafficPolicy.getOutlierDetection();
            PortalHealthCheckDTO healthCheck = trafficPolicy.getHealthCheck();
            PortalLoadBalancerDTO loadBalancer = trafficPolicy.getLoadbalancer();
            PortalServiceConnectionPoolDto serviceConnectionPool = trafficPolicy.getConnectionPool();

            if (outlierDetection != null) {
                s.setConsecutiveErrors(outlierDetection.getConsecutiveErrors());
                s.setBaseEjectionTime(outlierDetection.getBaseEjectionTime());
                s.setMaxEjectionPercent(outlierDetection.getMaxEjectionPercent());
            }

            if (healthCheck != null) {
                s.setPath(healthCheck.getPath());
                s.setTimeout(healthCheck.getTimeout());
                s.setExpectedStatuses(healthCheck.getExpectedStatuses());
                s.setHealthyInterval(healthCheck.getHealthyInterval());
                s.setHealthyThreshold(healthCheck.getHealthyThreshold());
                s.setUnhealthyInterval(healthCheck.getUnhealthyInterval());
                s.setUnhealthyThreshold(healthCheck.getUnhealthyThreshold());
            }

            if (loadBalancer != null) {
                s.setLoadBalancer(serviceLBDTO2ServiceLB(loadBalancer));
            }

            if (serviceConnectionPool != null) {
                s.setConnectionPool(serviceConnectionPool);
            }
        }

        s.setServiceTag(portalService.getServiceTag());
        s.setSubsets(subsetDTO2Subset(portalService.getSubsets()));
        return s;
    }

    private static Service.ServiceLoadBalancer serviceLBDTO2ServiceLB(PortalLoadBalancerDTO loadBalancerDTO) {

        if (loadBalancerDTO == null) return null;
        Service.ServiceLoadBalancer serviceLoadBalancer = new Service.ServiceLoadBalancer();

        if (!StringUtils.isEmpty(loadBalancerDTO.getSimple())) {
            // round robin, random, least conn
            serviceLoadBalancer.setSimple(loadBalancerDTO.getSimple());
        } else if (loadBalancerDTO.getConsistentHashDTO() != null){
            // consistent hash
            PortalLoadBalancerDTO.ConsistentHashDTO consistentHashDTO = loadBalancerDTO.getConsistentHashDTO();
            Service.ServiceLoadBalancer.ConsistentHash consistentHash = new Service.ServiceLoadBalancer.ConsistentHash();
            if (consistentHashDTO.getUseSourceIp() != null) {
                consistentHash.setUseSourceIp(consistentHashDTO.getUseSourceIp());
            } else if (!StringUtils.isEmpty(consistentHashDTO.getHttpHeaderName())) {
                consistentHash.setHttpHeaderName(consistentHashDTO.getHttpHeaderName());
            } else if (consistentHashDTO.getHttpCookie() != null) {
                PortalLoadBalancerDTO.ConsistentHashDTO.HttpCookieDTO httpCookieDTO = consistentHashDTO.getHttpCookie();
                Service.ServiceLoadBalancer.ConsistentHash.HttpCookie httpCookie = new Service.ServiceLoadBalancer.ConsistentHash.HttpCookie();
                httpCookie.setName(httpCookieDTO.getName());
                httpCookie.setPath(httpCookieDTO.getPath());
                httpCookie.setTtl(httpCookieDTO.getTtl());
                consistentHash.setHttpCookie(httpCookie);
            }
            serviceLoadBalancer.setConsistentHash(consistentHash);
        }
        return serviceLoadBalancer;
    }

    private static List<ServiceSubset> subsetDTO2Subset(List<ServiceSubsetDTO> subsets) {
        if (CollectionUtils.isEmpty(subsets)) return Collections.emptyList();
        return subsets.stream()
                    .map(sd -> {
                        ServiceSubset ss = new ServiceSubset();
                        ss.setLabels(sd.getLabels());
                        ss.setName(sd.getName());
                        ss.setTrafficPolicy(subsetTrafficPolicyDtoTosubsetTrafficPolicy(sd.getTrafficPolicy()));
                        return ss;
                    })
                    .collect(Collectors.toList());
    }

    /**
     * 主要是将subset中的ServiceLoadBalancer生成出来
     *
     * @param portalTrafficPolicyDTO
     * @return
     */
    private static ServiceSubset.TrafficPolicy subsetTrafficPolicyDtoTosubsetTrafficPolicy
            (PortalTrafficPolicyDTO portalTrafficPolicyDTO) {
        if (portalTrafficPolicyDTO == null) {
            return null;
        }

        ServiceSubset.TrafficPolicy trafficPolicy = new ServiceSubset.TrafficPolicy();
        trafficPolicy.setHealthCheck(portalTrafficPolicyDTO.getHealthCheck());
        trafficPolicy.setOutlierDetection(portalTrafficPolicyDTO.getOutlierDetection());
        trafficPolicy.setLoadbalancer(serviceLBDTO2ServiceLB(portalTrafficPolicyDTO.getLoadbalancer()));
        trafficPolicy.setConnectionPool(portalTrafficPolicyDTO.getConnectionPool());
        return trafficPolicy;
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

    public static API portalDeleteAPI2API(PortalAPIDeleteDTO portalAPI) {

        API api = new API();
        api.setGateways(Arrays.asList(portalAPI.getGateway().toLowerCase()));
        api.setName(portalAPI.getCode());
        api.setMethods(Collections.EMPTY_LIST);
        api.setUriMatch(UriMatch.REGEX);
        api.setRequestUris(Collections.EMPTY_LIST);
        api.setHosts(Collections.EMPTY_LIST);
        api.setProxyServices(ImmutableList.of(new Service()));
        api.setPlugins(portalAPI.getPlugins());
        return api;
    }

    public static GlobalPlugins globalPluginsDTO2GlobalPlugins(GlobalPluginsDTO globalPluginsDTO) {

        GlobalPlugins gp = new GlobalPlugins();
        gp.setCode(globalPluginsDTO.getCode());
        gp.setGateway(globalPluginsDTO.getGateway());
        gp.setHosts(globalPluginsDTO.getHosts());
        gp.setPlugins(globalPluginsDTO.getPlugins());
        return gp;
    }

    public static GlobalPlugins globalPluginsDeleteDTO2GlobalPlugins(GlobalPluginsDeleteDTO globalPluginsDeleteDTO) {

        GlobalPlugins gp = new GlobalPlugins();
        gp.setCode(globalPluginsDeleteDTO.getCode());
        gp.setPlugins(globalPluginsDeleteDTO.getPlugins());
        return gp;
    }
}
