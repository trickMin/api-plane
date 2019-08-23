package com.netease.cloud.nsf.core.gateway;

import com.google.common.collect.ImmutableList;
import com.netease.cloud.nsf.ApiPlaneApplication;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApiPlaneApplication.class)
@PropertySource("classpath:application.properties")
public class GatewayModelProcessorTest {

    @Autowired
    GatewayModelProcessor processor;

    @Autowired
    EditorContext editorContext;

    @MockBean
    KubernetesClient kubernetesClient;

//    @Test
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
                "    name: plane-istio-test\n" +
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
                "    name: plane-istio-test\n" +
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
                "    name: plane-istio-test\n" +
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
                "    name: plane-istio-test\n" +
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
                "    name: plane-istio-test-1\n" +
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
