package com.netease.cloud.nsf;

import com.netease.cloud.nsf.core.BaseTest;
import com.netease.cloud.nsf.meta.dto.GlobalPluginDTO;
import com.netease.cloud.nsf.meta.dto.HttpRetryDTO;
import com.netease.cloud.nsf.meta.dto.PortalAPIDTO;
import com.netease.cloud.nsf.meta.dto.PortalRouteServiceDTO;
import com.netease.cloud.nsf.service.GatewayService;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhufengwei.sx
 * @date 2021/8/11 9:21
 */
public class HeaderRestrictPluginTest extends BaseTest {

    @Autowired
    private GatewayService gatewayService;

    @Test
    public void testBindingRoutePlugin(){
        PortalAPIDTO portalAPIDTO = new PortalAPIDTO();
        portalAPIDTO.setGateway("prod-gateway");
        portalAPIDTO.setCode("2771");
        portalAPIDTO.setHosts(Lists.newArrayList("10.18.48.37","10.18.59.128"));
        portalAPIDTO.setRequestUris(Lists.newArrayList("/test"));
        portalAPIDTO.setUriMatch("exact");
        portalAPIDTO.setMethods(Lists.newArrayList("*"));
        PortalRouteServiceDTO portalRouteServiceDTO = new PortalRouteServiceDTO();
        portalRouteServiceDTO.setCode("DYNAMIC-3487");
        portalRouteServiceDTO.setBackendService("istio-e2e-app.apigw-demo.svc.cluster.local");
        portalRouteServiceDTO.setType("DYNAMIC");
        portalRouteServiceDTO.setWeight(100);
        portalRouteServiceDTO.setPort(80);
        portalAPIDTO.setProxyServices(Lists.newArrayList(portalRouteServiceDTO));
        portalAPIDTO.setPlugins(Lists.newArrayList("{\"type\":\"0\",\"lists\":[\"127.0.0.1\"],\"kind\":\"ip-restriction\"}",
                "{\"kind\":\"header-restriction\",\"config\":[{\"lists\":[\"163.0.0.1\"],\"name\":\"Path\",\"type\":\"0\"},{\"lists\":[\"test\",\"test1\"],\"name\":\"user-agent\",\"type\":\"0\"}]}"));
        portalAPIDTO.setPriority(50400681);
        portalAPIDTO.setServiceTag("testService");
        portalAPIDTO.setRouteId(2771L);
        portalAPIDTO.setProjectId("38");
        HttpRetryDTO httpRetryDTO = new HttpRetryDTO();
        httpRetryDTO.setRetry(Boolean.FALSE);
        httpRetryDTO.setAttempts(2);
        httpRetryDTO.setPerTryTimeout(6000L);
        portalAPIDTO.setHttpRetry(httpRetryDTO);

        gatewayService.updateAPI(portalAPIDTO);
    }

    @Test
    public void testBindingGlobalPlugin(){
        GlobalPluginDTO globalPluginDTO = new GlobalPluginDTO();
        globalPluginDTO.setPlugins(Lists.newArrayList("{\"kind\":\"header-restriction\",\"config\":[{\"lists\":[\"163.0.0.1\"],\"name\":\"Path\",\"type\":\"0\"},{\"lists\":[\"test\",\"test1\"],\"name\":\"user-agent\",\"type\":\"0\"}]}"));
        globalPluginDTO.setCode("project2-38-1");
        globalPluginDTO.setHosts(Lists.newArrayList("10.18.59.128"));
        globalPluginDTO.setGateway("prod-gateway");
        gatewayService.updateGlobalPlugins(globalPluginDTO);
    }
}
