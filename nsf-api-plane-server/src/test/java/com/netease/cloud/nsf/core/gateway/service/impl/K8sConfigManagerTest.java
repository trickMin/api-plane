package com.netease.cloud.nsf.core.gateway.service.impl;

import com.netease.cloud.nsf.core.BaseTest;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.Service;
import com.netease.cloud.nsf.meta.UriMatch;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class K8sConfigManagerTest extends BaseTest {

    @Autowired
    K8sGatewayConfigManager k8sConfigManager;

    @MockBean(name = "configStore")
    K8sConfigStore k8sConfigStore;

    @MockBean
    KubernetesClient client;

    @Before
    public void before() {
        when(k8sConfigStore.get(any(), any(), anyString())).thenReturn(null);
        when(client.getWithNull(anyString())).thenReturn(null);
    }

    @Test
    public void testUpdateAPI() {

        API api = buildAPI(list("gw1"), "apiName", list("host1"), list("/any"),
                list("GET"), "svc",
                list("{\"kind\":\"transformer\",\"headers\":[{\"key\":\"addHeaders\",\"text\":\"abc:{{headers[abc]}},def:{{headers[def]}}\",\"action\":\"Action_Default\"}], \"x_user_id\":\"abc\"}"),
                "HTTP",
                Arrays.asList(buildProxyService("www.163.com", "STATIC", 100, 80)),
                UriMatch.EXACT);

        k8sConfigManager.updateConfig(api);
    }

    @Test
    public void testDeleteAPI() {

        API api = buildAPI(list("gw1"), "apiName", list("host1"), list("/any"),
                list("GET"), "svc",
                list("{\"kind\":\"transformer\",\"headers\":[{\"key\":\"addHeaders\",\"text\":\"abc:{{headers[abc]}},def:{{headers[def]}}\",\"action\":\"Action_Default\"}], \"x_user_id\":\"abc\"}"),
                "HTTP",
                Arrays.asList(buildProxyService("www.163.com", "STATIC", 100, 80)),
                UriMatch.EXACT);

        k8sConfigManager.deleteConfig(api);
    }

    @Test
    public void testUpdateService() {

        Service service = buildProxyService(
                "httpbin.default.svc.cluster.local", "DYNAMIC", 100, 80,
                "gw1", "HTTP1", "SVC1", "serviceTag1");
        k8sConfigManager.updateConfig(service);
    }

    @Test
    public void testDeleteService() {
        Service service = buildProxyService(
                "httpbin.default.svc.cluster.local", "DYNAMIC", 100, 80,
                "gw1", "HTTP1", "SVC1", "serviceTag1");
        k8sConfigManager.deleteConfig(service);
    }

    private List<String> list(String s) {
        List<String> l = new ArrayList();
        l.add(s);
        return l;
    }

    private API buildAPI(List<String> gateways, String name, List<String> hosts, List<String> requestUris,
                         List<String> methods, String service, List<String> plugins, String protocol,
                         List<Service> proxyServices, UriMatch uriMatch) {

        API api = new API();
        api.setGateways(gateways);
        api.setName(name);
        api.setHosts(hosts);
        api.setRequestUris(requestUris);
        api.setMethods(methods);
        api.setService(service);
        api.setPlugins(plugins);
        api.setProtocol(protocol);
        api.setProxyServices(proxyServices);
        api.setUriMatch(uriMatch);
        return api;
    }

    private Service buildProxyService(String backendService, String type, Integer weight, Integer port) {

        return buildProxyService(backendService, type, weight, port, null, null ,null, null);
    }

    private Service buildProxyService(String backendService, String type, Integer weight, Integer port,
                                      String gateway, String protocol, String code, String serviceTag) {

        Service service = new Service();
        service.setBackendService(backendService);
        service.setType(type);
        service.setWeight(weight);
        service.setPort(port);
        service.setGateway(gateway);
        service.setProtocol(protocol);
        service.setCode(code);
        service.setServiceTag(serviceTag);
        return service;
    }
}