package com.netease.cloud.nsf.mcp;

import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.netease.cloud.nsf.ApiPlaneApplication;
import com.netease.cloud.nsf.core.BaseTest;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.mcp.snapshot.K8sSnapshotBuilder;
import com.netease.cloud.nsf.mcp.snapshot.SnapshotBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import istio.mcp.nsf.SnapshotOuterClass;
import istio.mcp.v1alpha1.ResourceOuterClass;
import istio.networking.v1alpha3.DestinationRuleOuterClass;
import istio.networking.v1alpha3.VirtualServiceOuterClass;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/14
 **/
@SpringBootTest(classes = ApiPlaneApplication.class, properties = {
        "enableMcpServer=true",
        "k8s.clusters.default.k8s-api-server=https://103.196.65.178:6443",
        "k8s.clusters.default.cert-data=LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM4akNDQWRxZ0F3SUJBZ0lJU1ByR1JjNVZDMjB3RFFZSktvWklodmNOQVFFTEJRQXdGVEVUTUJFR0ExVUUKQXhNS2EzVmlaWEp1WlhSbGN6QWVGdzB4T1RBNU1UY3dOekkzTURKYUZ3MHlNREE1TVRZd056STNNRFZhTURReApGekFWQmdOVkJBb1REbk41YzNSbGJUcHRZWE4wWlhKek1Sa3dGd1lEVlFRREV4QnJkV0psY201bGRHVnpMV0ZrCmJXbHVNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQW9lSTBJMlkyRk9rZm1meXAKZVV1eGNNeDlKa0tVYU5pRUpDeEI4WHd0eEt0SE9PN3VBUnpJTzJIT0kxWEN6MnFveGo2cjdjK1h6Vy9oNXN0UAo3UXM4cWVmN2NMbXlVK293MEhWbDlZZjBhcHBzWjdEQzh6R1ZCMDJFeFVjR1hyc3FtSXFTMExaWmtxZmdOdnVqCkdvaFhDTzZHbVFyejlqNWZ3b0N5eElnK002QU0xMERlQ01nSlg5MGN0OUhlZkJrVzREUk9hZmhPN0N5ZXhGcFgKZ1JsZEFzUEVXZ0QzT1d0NjJHa0J5SFl6KzNGeWpxY0taeWxqSWFIOWQ4Y1FmMjBBeithWkM5WmlTVGV3dFgrZAo5RHBNVHRpYWl3cFlGUkg2ZlFKSWFadnZkZGszYVovdi96b3dIQUtLMGtVZ0h0em5kTHZENlhhSGlHTTJuTEI1CnlNcWx6UUlEQVFBQm95Y3dKVEFPQmdOVkhROEJBZjhFQkFNQ0JhQXdFd1lEVlIwbEJBd3dDZ1lJS3dZQkJRVUgKQXdJd0RRWUpLb1pJaHZjTkFRRUxCUUFEZ2dFQkFFOU5xOU8rcklWM3YwTnhkcjNtUGpYTEJnNmhrd0RLSmxLQwo3QzFPbE1mLy81bFZHdjBUTkxJa1FrenBQb3QyNkNadUNvL20rT2xYZE5wZmRKOEFLUmozeHFmY2NkdjFlSk8wCnRhRlhzdGRGSTFnTFlwRHozZklEUkU0cFJtZzhpUXV1UTJidlJzYzhFYmd1K3RNdGRZZG5TblhHZHY4cTRmU28KVkFTamo2WkQ4VEMvTzlrYXpabVRiMENxWjh2Y004Z09URnM4NzhNSjA2bjRFSlN6RUpGVVF5U253Y0NnVEp0UQp3dUVlU1Ivc0dIL0Q4T2ExbGdKR0lTVjgyWTBsVGhWdU5pNG1EUnA5a0NUVHFLSG1SMm5mWVV2ZlhMeUswVTRHCnZoRVhpTnJKa0YxRUkxYmtneFRCRGtXNG1YVlh6MHpOcjBGY0lDQ2JLck5nemM2SjR6az0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=",
        "k8s.clusters.default.key-data=LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFb2dJQkFBS0NBUUVBb2VJMEkyWTJGT2tmbWZ5cGVVdXhjTXg5SmtLVWFOaUVKQ3hCOFh3dHhLdEhPTzd1CkFSeklPMkhPSTFYQ3oycW94ajZyN2MrWHpXL2g1c3RQN1FzOHFlZjdjTG15VStvdzBIVmw5WWYwYXBwc1o3REMKOHpHVkIwMkV4VWNHWHJzcW1JcVMwTFpaa3FmZ052dWpHb2hYQ082R21Rcno5ajVmd29DeXhJZytNNkFNMTBEZQpDTWdKWDkwY3Q5SGVmQmtXNERST2FmaE83Q3lleEZwWGdSbGRBc1BFV2dEM09XdDYyR2tCeUhZeiszRnlqcWNLClp5bGpJYUg5ZDhjUWYyMEF6K2FaQzlaaVNUZXd0WCtkOURwTVR0aWFpd3BZRlJINmZRSklhWnZ2ZGRrM2FaL3YKL3pvd0hBS0swa1VnSHR6bmRMdkQ2WGFIaUdNMm5MQjV5TXFselFJREFRQUJBb0lCQUFsMG9QOEFWV0JiVFpFOQpCTmtrNXJSai9WdGl3REJ2bFNoWHZYSlJnb3JlUmthNURnWGFuSWQ3YXdMOVcyZXFIem1WSjQyNGRuRjFlNGh6Cmo3T1UySEpFTHBlakFSdU5ybFEreHpuVU94Nk83bzRQOFJNcmJqMEM5aUpmeDZ0NDIwQVZ1QVdnNFJLRDQ4RFMKYWZCK3pqYWpXQkZRWm5lWHBSaERWQzNPbDNPRWFlcDNCUFBlcVJlbVhlZjkxcE9DUlcrazVla3hmYjFMM2hPdwpzcUk2Y1MzblZ3aThIMW9iSXVHV0RFb2VwOFY3Q0hVWm9EN2ZIMWVxYXFhRW1PVVFMdDNNbUdqMUJUa0VKOW1MCk8zbk9BL3lRL0l6MTFLbGFPQlQrU21HN3I3bDFVaWlXNkJkNXZZTXBPeUh1RkxwbGVZaWlFQ3NZS2FtbkRvN2QKTG1EcUh3RUNnWUVBd011MHdaM1J0V2R3S1ZzSjdCMzFWSzJiNDZDUExtSEFxaE5CRCtFMlBzYjBBY0pURXB4NApOcTFDUEFBRURoeWsvZWNSL25TSTE5Vy9STzBvR2JtNTVDbFg2STZ4WFk0Y3FXbHVVUytyb1dhdmZHUTZlYzRCCjN5UjF1aEJhZVZlcVpydGprZWVacnllVU5JVTV4ZGc5MHh2UjhCUnordjRHa1pqbE9jKzhYbEVDZ1lFQTF2UTIKU0JoVWJMR1MxUG9XOUs5blg2RHErTERYSkYzb3AybkI2eVAySm9ESzRlS2k2MnRMdmw1ekp5U0pqOHJRMzZXUwpBNTgxWlIvbHhxZ1l0NGZWbjl5Zy85VlRKcEZJY2k4OUdpUDB1OXE0YktyTWpMNXhwanVBQm5TcmovMDF5N3pnCmtZK2M5Y3NOV1FnM0M1a2g1SmRFdVlvV1N5WUFFeGp4citxVHhMMENnWUJKbzRUL0R4Q3lnVGZPRS8vT09BTWsKdk1yMlByVnh1ZFNsSVlXUVV5MHMvVURtQ3hzLzJKUHlEbXRtRDN0OUNHUGN0dzJnYTFKNVhpTVlhSjBRZG1nUwpSZGhWODJxN21UUkpZUVBKN3JOWGlxa3NrZnZqMmxQTXIxaG9JU2J0Z2hhTDlyY3BXNVRQMmdZNXFVbFAwRENOCkdlc3VFQ0hjbEhwZDBsQjdyR1QrZ1FLQmdIMW9Edjh5eXBaN1p4bkFLb0pvanh2WWl4MjZObHB1TzF6b3M0MFcKOXZYeHIrdm5TSHVtcHBNVEVZV2xxR2Z1d0x1cUlpemd4c3NQdUFPVmNJNHF2blE4eU9WWk5PRE1aQUxTRW9qYwpyMXlsdDNFV21LNElNYy8wNkxWYmZmaE1sd1VkVmJzMm1URkJYTEV6dk1HVWZwU1p1Q3V6SjF5Q2VBcXNROTBKCk0xclZBb0dBREt4UnVhc2IrQThMaTVkV3FHeS9Ga1hiVmVtZHhSWmxlQWxxaEZSNU9hVmRmeFpQYnhtY21VUS8KMGhQNTR6dEd3bWtaMEgvOGxPdk5iSXA1b0todWFQSFFoblRyVzNKbWJhajJaK21ydUZ5ZE4zZTZsOVlGZFNLbQorRUpkbkl4TFpRNUxRWU1WRlQ4d2duNHpjNk5uaFErNW9OUkt1L3JDTTY0Sng2KytnRW89Ci0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0tCg=="
})
public class ProtoFormatTest extends BaseTest {
    private JsonFormat.Printer printer = JsonFormat.printer();
    private JsonFormat.Parser parser = JsonFormat.parser();

    @Test
    public void testJson2Vs() throws IOException {
        VirtualServiceOuterClass.VirtualService.Builder builder = VirtualServiceOuterClass.VirtualService.newBuilder();
        String vs = "{\n" +
                "  \"gateways\": [\n" +
                "    \"gateway-proxy-1\"\n" +
                "  ],\n" +
                "  \"hosts\": [\n" +
                "    \"*\"\n" +
                "  ],\n" +
                "  \"http\": [\n" +
                "    {\n" +
                "      \"api\": \"httpbin-httpbin\",\n" +
                "      \"appendResponseHeaders\": {\n" +
                "        \"addheaders\": \"return\"\n" +
                "      },\n" +
                "      \"match\": [\n" +
                "        {\n" +
                "          \"headers\": {\n" +
                "            \":authority\": {\n" +
                "              \"regex\": \"httpbin\\\\.com\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"method\": {\n" +
                "            \"regex\": \"GET|POST\"\n" +
                "          },\n" +
                "          \"uri\": {\n" +
                "            \"regex\": \".*\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"priority\": 42,\n" +
                "      \"retries\": {\n" +
                "        \"attempts\": 5\n" +
                "      },\n" +
                "      \"return\": {\n" +
                "        \"body\": {\n" +
                "          \"inlineString\": \"123\"\n" +
                "        },\n" +
                "        \"code\": 403\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"api\": \"httpbin-httpbin\",\n" +
                "      \"match\": [\n" +
                "        {\n" +
                "          \"headers\": {\n" +
                "            \":authority\": {\n" +
                "              \"regex\": \"httpbin\\\\.com\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"method\": {\n" +
                "            \"regex\": \"GET|POST\"\n" +
                "          },\n" +
                "          \"uri\": {\n" +
                "            \"regex\": \".*\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"meta\": {\n" +
                "        \"qz_cluster_name\": \"gateway-proxy-1\"\n" +
                "      },\n" +
                "      \"priority\": 42,\n" +
                "      \"retries\": {\n" +
                "        \"attempts\": 5\n" +
                "      },\n" +
                "      \"route\": [\n" +
                "        {\n" +
                "          \"destination\": {\n" +
                "            \"host\": \"httpbin.apiplane-test.svc.cluster.local\",\n" +
                "            \"port\": {\n" +
                "              \"number\": 8000\n" +
                "            },\n" +
                "            \"subset\": \"httpbin-httpbin-gateway-proxy-1\"\n" +
                "          },\n" +
                "          \"weight\": 100\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"api\": \"httpbin\",\n" +
                "      \"ext\": [\n" +
                "        {\n" +
                "          \"name\": \"com.netease.rewrite\",\n" +
                "          \"settings\": {\n" +
                "            \"request_transformations\": [\n" +
                "              {\n" +
                "                \"transformation_template\": {\n" +
                "                  \"extractors\": {\n" +
                "                    \"$1\": {\n" +
                "                      \"header\": \":path\",\n" +
                "                      \"regex\": \"/rewrite/(.*)/(.*)\",\n" +
                "                      \"subgroup\": 1\n" +
                "                    },\n" +
                "                    \"$2\": {\n" +
                "                      \"header\": \":path\",\n" +
                "                      \"regex\": \"/rewrite/(.*)/(.*)\",\n" +
                "                      \"subgroup\": 2\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"headers\": {\n" +
                "                    \":path\": {\n" +
                "                      \"text\": \"/anything/{{$2}}/{{$1}}\"\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"parse_body_behavior\": \"DontParse\"\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"match\": [\n" +
                "        {\n" +
                "          \"headers\": {\n" +
                "            \":authority\": {\n" +
                "              \"regex\": \"httpbin\\\\.com\"\n" +
                "            },\n" +
                "            \"plugin\": {\n" +
                "              \"regex\": \"^rewrite$\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"method\": {\n" +
                "            \"regex\": \"GET|POST\"\n" +
                "          },\n" +
                "          \"uri\": {\n" +
                "            \"regex\": \"(?:.*.*)\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"priority\": 43,\n" +
                "      \"retries\": {\n" +
                "        \"attempts\": 5\n" +
                "      },\n" +
                "      \"route\": [\n" +
                "        {\n" +
                "          \"destination\": {\n" +
                "            \"host\": \"httpbin.apiplane-test.svc.cluster.local\",\n" +
                "            \"port\": {\n" +
                "              \"number\": 8000\n" +
                "            },\n" +
                "            \"subset\": \"httpbin-httpbin-gateway-proxy-1\"\n" +
                "          },\n" +
                "          \"weight\": 100\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"api\": \"httpbin\",\n" +
                "      \"match\": [\n" +
                "        {\n" +
                "          \"headers\": {\n" +
                "            \":authority\": {\n" +
                "              \"regex\": \"httpbin\\\\.com\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"method\": {\n" +
                "            \"regex\": \"GET|POST\"\n" +
                "          },\n" +
                "          \"uri\": {\n" +
                "            \"regex\": \".*\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"meta\": {\n" +
                "        \"qz_cluster_name\": \"gateway-proxy-1\"\n" +
                "      },\n" +
                "      \"priority\": 42,\n" +
                "      \"retries\": {\n" +
                "        \"attempts\": 5\n" +
                "      },\n" +
                "      \"route\": [\n" +
                "        {\n" +
                "          \"destination\": {\n" +
                "            \"host\": \"httpbin.apiplane-test.svc.cluster.local\",\n" +
                "            \"port\": {\n" +
                "              \"number\": 8000\n" +
                "            },\n" +
                "            \"subset\": \"httpbin-httpbin-gateway-proxy-1\"\n" +
                "          },\n" +
                "          \"weight\": 100\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        parser.merge(vs, builder);
        VirtualServiceOuterClass.VirtualService virtualService = builder.build();
        String json = printer.print(virtualService);
        JsonParser parser = new JsonParser();
        Assert.assertEquals(parser.parse(vs), parser.parse(json));

        VirtualServiceOuterClass.VirtualService v = ProtoUtils.jsonMarshaller(virtualService).parse(new ByteArrayInputStream(vs.getBytes()));
        InputStream is = ProtoUtils.jsonMarshaller(virtualService).stream(v);
        StringBuilder sb = new StringBuilder();
        byte[] buff = new byte[1024];
        for (int i = 0; (i = is.read(buff)) != -1; ) {
            sb.append(new String(buff, 0, i));
        }
        String json2 = sb.toString();
        Assert.assertEquals(json, json2);
    }

    @Autowired
    private KubernetesClient kubernetesClient;
    @Autowired
    private McpMarshaller mcpMarshaller;

    @Test
    public void testBuildK8sSnapshot() {
        SnapshotBuilder builder = new K8sSnapshotBuilder(kubernetesClient);
        SnapshotOuterClass.Snapshot snapshot = builder.build();
        String json = mcpMarshaller.print(snapshot);
    }
}
