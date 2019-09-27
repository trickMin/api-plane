package com.netease.cloud.nsf.core.gateway;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.core.BaseTest;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.istio.IstioHttpClient;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.meta.*;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.dto.PluginOrderDTO;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.Trans;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class GatewayModelProcessorTest extends BaseTest {

    @Autowired
    GatewayModelOpeartor processor;

    @Autowired
    EditorContext editorContext;

    @MockBean
    KubernetesClient kubernetesClient;

    @MockBean
    IstioHttpClient istioHttpClient;

    @Test
    public void testTranslateAPI() {

        Endpoint endpoint1 = new Endpoint();
        endpoint1.setHostname("a.default.svc.cluster.local");
        endpoint1.setPort(9090);

        Endpoint endpoint2 = new Endpoint();
        endpoint2.setHostname("b.default.svc.cluster.local");
        endpoint2.setPort(9000);

        when(istioHttpClient.getEndpointList()).thenReturn(Arrays.asList(endpoint1, endpoint2));

        //base api test
        API api = new API();
        api.setGateways(ImmutableList.of("gateway1", "gateway2"));
        api.setName("api-name");
        api.setHosts(ImmutableList.of("service-a", "service-b"));
        api.setRequestUris(ImmutableList.of("/a", "/b"));
        api.setMethods(ImmutableList.of("HTTP", "POST"));
        api.setProxyUris(ImmutableList.of("a.default.svc.cluster.local", "b.default.svc.cluster.local"));
        api.setService("service-zero");
        api.setUriMatch(UriMatch.PREFIX);

        List<IstioResource> resources = processor.translate(api, "gateway-system");

        Assert.assertTrue(resources.size() == 6);

        resources.stream()
                .forEach(r -> {
                    if (r.getKind().equals(VirtualService.class.getSimpleName())) {
                        VirtualService vs = (VirtualService) r;
                        HTTPRoute httpRoute = vs.getSpec().getHttp().get(0);
                        HTTPMatchRequest match = httpRoute.getMatch().get(0);

                        RegexMatchType methodMatch = (RegexMatchType) match.getMethod().getMatchType();
                        Assert.assertTrue(methodMatch.getRegex().equals("HTTP|POST"));

                        RegexMatchType uriMatch = (RegexMatchType) match.getUri().getMatchType();
                        Assert.assertTrue(uriMatch.getRegex().equals("/a.*|/b.*"));
                    }
                });


        API api1 = new API();
        api1.setGateways(ImmutableList.of("gateway1"));
        api1.setName("api-name");
        api1.setHosts(ImmutableList.of("service-a", "service-b"));
        api1.setRequestUris(ImmutableList.of("/a", "/b"));
        api1.setMethods(ImmutableList.of("HTTP", "POST"));
        api1.setUriMatch(UriMatch.REGEX);

        Service s1 = new Service();
        s1.setType(Const.PROXY_SERVICE_TYPE_DYNAMIC);
        s1.setBackendService("a.default.svc.cluster.local");
        s1.setWeight(33);
        s1.setCode("DYNAMIC_1");

        Service s2 = new Service();
        s2.setType(Const.PROXY_SERVICE_TYPE_STATIC);
        s2.setBackendService("www.baidu.com");
        s2.setWeight(33);
        s2.setCode("STATIC_1");

        Service s3 = new Service();
        s3.setType(Const.PROXY_SERVICE_TYPE_STATIC);
        s3.setBackendService("10.10.10.10:1024");
        s3.setWeight(34);
        s3.setCode("STATIC_2");

        api1.setProxyServices(Arrays.asList(s1, s2, s3));

        List<IstioResource> resources1 = processor.translate(api1, "gateway-system");

        resources1.stream()
                .forEach(r -> {
                    Assert.assertFalse(r.getKind().equals(K8sResourceEnum.DestinationRule.name()));
                    if (r.getKind().equals(VirtualService.class.getSimpleName())) {
                        VirtualService vs = (VirtualService) r;
                        HTTPRoute httpRoute = vs.getSpec().getHttp().get(0);
                        Assert.assertTrue(httpRoute.getRoute().size() == 3);
                    }

                });


        System.out.println(resources1);
    }

    @Test
    public void testTranslatePluginManager() {

        String namespace = "gateway-system";

        PluginOrderDTO po = new PluginOrderDTO();
        po.setGatewayLabels(ImmutableMap.of("k1","v1", "k2", "v2"));
        po.setPlugins(ImmutableList.of("p1", "p2", "p3"));

        List<IstioResource> res = processor.translate(Trans.pluginOrderDTO2PluginOrder(po), namespace);

        Assert.assertTrue(res.size() == 1);

        PluginManager pm = (PluginManager) res.get(0);

        Assert.assertTrue(pm.getSpec().getWorkloadLabels().size() == 2);
        Assert.assertTrue(pm.getSpec().getPlugins().size() == 3);

        PluginOrderDTO po1 = new PluginOrderDTO();
        po1.setPlugins(ImmutableList.of("p1", "p2"));

        List<IstioResource> res1 = processor.translate(Trans.pluginOrderDTO2PluginOrder(po1), namespace);

        Assert.assertTrue(res1.size() == 1);

        PluginManager pm1 = (PluginManager) res1.get(0);
        Assert.assertTrue(CollectionUtils.isEmpty(pm1.getSpec().getWorkloadLabels()));
        Assert.assertTrue(pm1.getMetadata().getName().equals("qz-global"));

    }

    @Test
    public void testTranslateService() {

        Service service = new Service();
        service.setCode("a");
        service.setGateway("gw1");
        service.setBackendService("a.svc.cluster");
        service.setType(Const.PROXY_SERVICE_TYPE_DYNAMIC);

        List<IstioResource> istioResources = processor.translate(service, "gateway-system");

        Assert.assertTrue(istioResources.size() == 1);
        DestinationRule ds = (DestinationRule) istioResources.get(0);
        DestinationRuleSpec spec = ds.getSpec();
        Assert.assertTrue(spec.getHost().equals(service.getBackendService()));

    }

    @Test
    public void subtractTest() {

        String vsYaml = "kind: VirtualService\n" +
                "metadata:\n" +
                "  creationTimestamp: 2019-08-20T12:41:10Z\n" +
                "  generation: 1\n" +
                "  labels:\n" +
                "    api_service: service-zero\n" +
                "  name: service-zero-gateway-yx\n" +
                "  namespace: gateway-system\n" +
                "  resourceVersion: \"30048280\"\n" +
                "  selfLink: /apis/networking.istio.io/v1alpha3/namespaces/gateway-system/virtualservices/service-zero-gateway-yx\n" +
                "  uid: c98eacf7-c347-11e9-8a87-fa163e5fcbdd\n" +
                "spec:\n" +
                "  gateways:\n" +
                "  - service-zero-gateway-yx\n" +
                "  hosts:\n" +
                "  - www.test.com\n" +
                "  http:\n" +
                "  - match:\n" +
                "    - headers:\n" +
                "        plugin:\n" +
                "          regex: rewrite\n" +
                "      method:\n" +
                "        regex: GET|POST\n" +
                "      queryParams:\n" +
                "        plugin:\n" +
                "          regex: rewrite\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    api: plane-istio-test\n" +
                "    requestTransform:\n" +
                "      new:\n" +
                "        path: /{{backendUrl}}\n" +
                "      original:\n" +
                "        path: /rewrite/{backendUrl}\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 100\n" +
                "  - match:\n" +
                "    - headers:\n" +
                "        Cookie:\n" +
                "          regex: .*(?:;|^)plugin=r.*(?:;|$).*\n" +
                "        plugin:\n" +
                "          regex: redirect\n" +
                "      method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    api: plane-istio-test\n" +
                "    redirect:\n" +
                "      uri: /redirect\n" +
                "  - match:\n" +
                "    - headers:\n" +
                "        plugin:\n" +
                "          regex: return\n" +
                "      method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    api: plane-istio-test\n" +
                "    return:\n" +
                "      body:\n" +
                "        inlineString: '{is return plugin}'\n" +
                "      code: 403\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 100\n" +
                "  - match:\n" +
                "    - method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: (?:.*.*)\n" +
                "    api: plane-istio-test\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 30\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 70\n" +
                "  - match:\n" +
                "    - method:\n" +
                "        regex: GET|POST\n" +
                "      uri:\n" +
                "        regex: .*\n" +
                "    api: plane-istio-test-1\n" +
                "    route:\n" +
                "    - destination:\n" +
                "        host: productpage.default.svc.cluster.local\n" +
                "        port:\n" +
                "          number: 9080\n" +
                "        subset: service-zero-plane-istio-test-gateway-yx\n" +
                "      weight: 100";

        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(vsYaml, ResourceType.YAML, editorContext);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        IstioResource vs = (IstioResource) gen.object(resourceEnum.mappingType());

        VirtualService subtractedVs = (VirtualService) processor.subtract(vs, "service-zero", "plane-istio-test");

        Assert.assertTrue(subtractedVs.getSpec().getHttp().size() == 1);
    }

}
