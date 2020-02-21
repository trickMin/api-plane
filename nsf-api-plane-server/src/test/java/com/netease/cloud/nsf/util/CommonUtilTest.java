package com.netease.cloud.nsf.util;

import com.netease.cloud.nsf.meta.Service;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
public class CommonUtilTest {



    @Test
    public void testValidIpPort() {

        String ip1 = "10.10.10.10:2131";
        String ip2 = "256.256.256.213:A";
        String ip3 = "10.10.10.10:65536";
        String ip4 = "10.10.10.10";

        Assert.assertTrue(CommonUtil.isValidIPPortAddr(ip1));
        Assert.assertTrue(!CommonUtil.isValidIPPortAddr(ip2));
        Assert.assertTrue(!CommonUtil.isValidIPPortAddr(ip3));
        Assert.assertTrue(!CommonUtil.isValidIPPortAddr(ip4));
    }

    @Test
    public void testHost2Regex() {

        String h1 = "*.163.com";
        String h2 = "www.*.com";

        String add1 = "a.163.com";
        String add2 = ".163.com";
        String add3 = "www.163.com";
        String add4 = "www.com";

        Pattern p1 = Pattern.compile(CommonUtil.host2Regex(h1));
        Pattern p2 = Pattern.compile(CommonUtil.host2Regex(h2));

        Assert.assertTrue(p1.matcher(add1).find());
        Assert.assertTrue(!p1.matcher(add2).find());
        Assert.assertTrue(p1.matcher(add3).find());
        Assert.assertTrue(p2.matcher(add3).find());
        Assert.assertTrue(!p2.matcher(add4).find());

    }

    @Test
    public void testObj2Yaml() {

        Service.ServiceLoadBalancer lb = new Service.ServiceLoadBalancer();
        lb.setSimple("RANDOM");
        Service.ServiceLoadBalancer.ConsistentHash consistentHash = new Service.ServiceLoadBalancer.ConsistentHash();
        consistentHash.setHttpHeaderName("thisisheader");
        lb.setConsistentHash(consistentHash);

        Service.ServiceLoadBalancer.ConsistentHash.HttpCookie cookie = new Service.ServiceLoadBalancer.ConsistentHash.HttpCookie();
        cookie.setName("na");
        cookie.setPath("path");
        cookie.setTtl(30);

        consistentHash.setHttpCookie(cookie);

        Assert.assertNotNull(CommonUtil.obj2yaml(lb));
    }

    @Test
    public void testYaml2Obj() {

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

        VirtualService virtualService = CommonUtil.yaml2Obj(vsYaml, VirtualService.class);
        Assert.assertNotNull(virtualService);
    }
}
