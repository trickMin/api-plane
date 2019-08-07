package com.netease.cloud.nsf.core.gateway;

import com.google.common.collect.ImmutableList;
import com.netease.cloud.nsf.ApiPlaneApplication;
import com.netease.cloud.nsf.meta.API;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApiPlaneApplication.class)
public class GatewayModelProcessorTest {

    @Autowired
    GatewayModelProcessor processor;

    @Test
    public void translate() {

        API api = new API();
        api.setGateways(ImmutableList.of("gateway1", "gateway2"));
        api.setName("api-name");
        api.setHosts(ImmutableList.of("service-a", "service-b"));
        api.setRequestUris(ImmutableList.of("/a", "/b"));
        api.setMethods(ImmutableList.of("HTTP", "POST"));
        api.setProxyUris(ImmutableList.of("a.default.svc.cluster.local", "b.default.svc.cluster.local"));
        api.setService("service-zero");

        System.out.println(processor.translate(api, "gateway-system"));
    }
}
