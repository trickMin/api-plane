package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.BaseTest;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.junit.Before;

public class BasePluginTest extends BaseTest {

    ServiceInfo serviceInfo = new ServiceInfo();
    ServiceInfo nullInfo = new ServiceInfo();

    @Before
    public void before() {
        serviceInfo.setApiName("api");
        serviceInfo.setHosts("hosts1");
        serviceInfo.setMethod("HTTP");
        serviceInfo.setPriority("100");
        serviceInfo.setSubset("sb1");
        serviceInfo.setServiceName("svvc");
        serviceInfo.setGateway("proxy");
        serviceInfo.setUri("/uri");

        nullInfo.setApiName(null);
        nullInfo.setHosts(null);
        nullInfo.setMethod(null);
        nullInfo.setPriority(null);
        nullInfo.setSubset(null);
        nullInfo.setServiceName(null);
        nullInfo.setGateway(null);
        nullInfo.setUri(null);
    }
}
