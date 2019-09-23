package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.core.BaseTest;
import com.netease.cloud.nsf.core.istio.IstioHttpClient;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;


public class IstioHttpClientTest extends BaseTest {

    @Autowired
    IstioHttpClient istioHttpClient;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    KubernetesClient k8sClient;

    @Test
    public void getEndpointList() {

        String resp = "[\n" +
                "{\n" +
                "    \"type\": \"virtual-service\",\n" +
                "    \"group\": \"networking.istio.io\",\n" +
                "    \"version\": \"v1alpha3\",\n" +
                "    \"name\": \"fortioclient\",\n" +
                "    \"namespace\": \"twopods\",\n" +
                "    \"domain\": \"cluster.local\",\n" +
                "    \"annotations\": {\n" +
                "      \"kubectl.kubernetes.io/last-applied-configuration\": \"{\\\"apiVersion\\\":\\\"networking.istio.io/v1alpha3\\\",\\\"kind\\\":\\\"VirtualService\\\",\\\"metadata\\\":{\\\"annotations\\\":{},\\\"name\\\":\\\"fortioclient\\\",\\\"namespace\\\":\\\"twopods\\\"},\\\"spec\\\":{\\\"gateways\\\":[\\\"fortio-gateway\\\"],\\\"hosts\\\":[\\\"fortioclient.local\\\"],\\\"http\\\":[{\\\"route\\\":[{\\\"destination\\\":{\\\"host\\\":\\\"fortioclient\\\",\\\"port\\\":{\\\"number\\\":8080}}}]}]}}\\n\"\n" +
                "    },\n" +
                "    \"resourceVersion\": \"11341534\",\n" +
                "    \"creationTimestamp\": \"2019-06-12T11:56:35Z\",\n" +
                "    \"Spec\": {\n" +
                "      \"hosts\": [\n" +
                "        \"fortioclient.local\"\n" +
                "      ],\n" +
                "      \"gateways\": [\n" +
                "        \"fortio-gateway\"\n" +
                "      ],\n" +
                "      \"http\": [\n" +
                "        {\n" +
                "          \"route\": [\n" +
                "            {\n" +
                "              \"destination\": {\n" +
                "                \"host\": \"fortioclient\",\n" +
                "                \"port\": {\n" +
                "                  \"Port\": {\n" +
                "                    \"Number\": 8080\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "{\n" +
                "    \"type\": \"virtual-service\",\n" +
                "    \"group\": \"networking.istio.io\",\n" +
                "    \"version\": \"v1alpha3\",\n" +
                "    \"name\": \"fortioserver\",\n" +
                "    \"namespace\": \"twopods\",\n" +
                "    \"domain\": \"cluster.local\",\n" +
                "    \"annotations\": {\n" +
                "      \"kubectl.kubernetes.io/last-applied-configuration\": \"{\\\"apiVersion\\\":\\\"networking.istio.io/v1alpha3\\\",\\\"kind\\\":\\\"VirtualService\\\",\\\"metadata\\\":{\\\"annotations\\\":{},\\\"name\\\":\\\"fortioserver\\\",\\\"namespace\\\":\\\"twopods\\\"},\\\"spec\\\":{\\\"gateways\\\":[\\\"fortio-gateway\\\"],\\\"hosts\\\":[\\\"fortioserver.local\\\"],\\\"http\\\":[{\\\"route\\\":[{\\\"destination\\\":{\\\"host\\\":\\\"fortioserver\\\",\\\"port\\\":{\\\"number\\\":8080}}}]}]}}\\n\"\n" +
                "    },\n" +
                "    \"resourceVersion\": \"11341509\",\n" +
                "    \"creationTimestamp\": \"2019-06-12T11:56:34Z\",\n" +
                "    \"Spec\": {\n" +
                "      \"hosts\": [\n" +
                "        \"fortioserver.local\"\n" +
                "      ],\n" +
                "      \"gateways\": [\n" +
                "        \"fortio-gateway\"\n" +
                "      ],\n" +
                "      \"http\": [\n" +
                "        {\n" +
                "          \"route\": [\n" +
                "            {\n" +
                "              \"destination\": {\n" +
                "                \"host\": \"httpbin.default.svc.cluster.local\",\n" +
                "                \"port\": {\n" +
                "                  \"Port\": {\n" +
                "                    \"Number\": 8080\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "{\n" +
                "    \"type\": \"destination-rule\",\n" +
                "    \"group\": \"networking.istio.io\",\n" +
                "    \"version\": \"v1alpha3\",\n" +
                "    \"name\": \"httpbin\",\n" +
                "    \"namespace\": \"gateway-system\",\n" +
                "    \"domain\": \"cluster.local\",\n" +
                "    \"labels\": {\n" +
                "      \"api_service\": \"publishapi\"\n" +
                "    },\n" +
                "    \"resourceVersion\": \"30561762\",\n" +
                "    \"creationTimestamp\": \"2019-08-13T05:46:23Z\",\n" +
                "    \"Spec\": {\n" +
                "      \"host\": \"httpbin.default.svc.cluster.local\",\n" +
                "      \"subsets\": [\n" +
                "        {\n" +
                "          \"name\": \"publishapi-qingzhoupublishapit5-gateway-yx\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "{\n" +
                "    \"type\": \"destination-rule\",\n" +
                "    \"group\": \"networking.istio.io\",\n" +
                "    \"version\": \"v1alpha3\",\n" +
                "    \"name\": \"ratings\",\n" +
                "    \"namespace\": \"gateway-system\",\n" +
                "    \"domain\": \"cluster.local\",\n" +
                "    \"resourceVersion\": \"27321911\",\n" +
                "    \"creationTimestamp\": \"2019-08-12T07:23:34Z\",\n" +
                "    \"Spec\": {\n" +
                "      \"host\": \"ratings.default.svc.cluster.local\",\n" +
                "      \"subsets\": [\n" +
                "        {\n" +
                "          \"name\": \"service-zero-plane-istio-test-gateway-yx\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "{\n" +
                "    \"type\": \"destination-rule\",\n" +
                "    \"group\": \"networking.istio.io\",\n" +
                "    \"version\": \"v1alpha3\",\n" +
                "    \"name\": \"productpage\",\n" +
                "    \"namespace\": \"gateway-system\",\n" +
                "    \"domain\": \"cluster.local\",\n" +
                "    \"labels\": {\n" +
                "      \"api_service\": \"service-zero\"\n" +
                "    },\n" +
                "    \"resourceVersion\": \"30353934\",\n" +
                "    \"creationTimestamp\": \"2019-08-22T10:46:00Z\",\n" +
                "    \"Spec\": {\n" +
                "      \"host\": \"productpage.default.svc.cluster.local\",\n" +
                "      \"subsets\": [\n" +
                "        {\n" +
                "          \"name\": \"service-zero-plane-istio-test-gateway-yx\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "{\n" +
                "    \"type\": \"gateway\",\n" +
                "    \"group\": \"networking.istio.io\",\n" +
                "    \"version\": \"v1alpha3\",\n" +
                "    \"name\": \"fortio-gateway\",\n" +
                "    \"namespace\": \"twopods\",\n" +
                "    \"domain\": \"cluster.local\",\n" +
                "    \"Spec\": {\n" +
                "      \"servers\": [\n" +
                "        {\n" +
                "          \"port\": {\n" +
                "            \"number\": 80,\n" +
                "            \"protocol\": \"HTTP\",\n" +
                "            \"name\": \"http\"\n" +
                "          },\n" +
                "          \"hosts\": [\n" +
                "            \"fortioserver.local\",\n" +
                "            \"fortioclient.local\"\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"port\": {\n" +
                "            \"number\": 443,\n" +
                "            \"protocol\": \"HTTPS\",\n" +
                "            \"name\": \"https-fortio\"\n" +
                "          },\n" +
                "          \"hosts\": [\n" +
                "            \"*\"\n" +
                "          ],\n" +
                "          \"tls\": {\n" +
                "            \"mode\": 1,\n" +
                "            \"server_certificate\": \"/etc/istio/ingressgateway-certs/tls.crt\",\n" +
                "            \"private_key\": \"/etc/istio/ingressgateway-certs/tls.key\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"selector\": {\n" +
                "        \"app\": \"ok\"\n" +
                "      }\n" +
                "    }\n" +
                "  }," +
                "{}]";

        ResponseEntity entity = new ResponseEntity(resp, HttpStatus.OK);

        Pod p = new Pod();
        PodStatus ps = new PodStatus();
        ps.setPodIP("127.0.0.1");
        p.setStatus(ps);

        when(k8sClient.getObjectList(any(),any(),any())).thenReturn(Arrays.asList(p));
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(entity);

//        List<Endpoint> endpoints = istioHttpClient.getEndpointList(Arrays.asList("app:ok"));

//        Assert.assertNotNull(endpoints);
//        Assert.assertTrue(endpoints.size() == 1);
//        Assert.assertTrue(endpoints.get(0).getHostname().equals("httpbin.default.svc.cluster.local"));
    }
}
