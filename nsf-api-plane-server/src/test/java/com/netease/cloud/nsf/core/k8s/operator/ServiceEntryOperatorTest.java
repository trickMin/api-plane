package com.netease.cloud.nsf.core.k8s.operator;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.util.CommonUtil;
import me.snowdrop.istio.api.networking.v1alpha3.Endpoint;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntry;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntrySpec;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceEntryOperatorTest {

    @Test
    public void testMerge() {

        ServiceEntryOperator operator = new ServiceEntryOperator();

        String yaml1 = "apiVersion: networking.istio.io/v1alpha3\n" +
                "kind: ServiceEntry\n" +
                "metadata:\n" +
                "  name: static-7288\n" +
                "  namespace: gateway-system\n" +
                "spec:\n" +
                "  endpoints:\n" +
                "  - address: test.a\n" +
                "    labels:\n" +
                "      gw_cluster: gw1\n" +
                "  - address: test.b\n" +
                "    labels:\n" +
                "      gw_cluster: gw1\n" +
                "  - address: test.c\n" +
                "    labels:\n" +
                "      gw_cluster: gw2\n" +
                "  exportTo:\n" +
                "  - '*'\n" +
                "  hosts:\n" +
                "  - testhost\n" +
                "  location: MESH_EXTERNAL\n" +
                "  ports:\n" +
                "  - name: http\n" +
                "    number: 80\n" +
                "    protocol: HTTP\n" +
                "  resolution: DNS\n";

        String yaml2 = "apiVersion: networking.istio.io/v1alpha3\n" +
                "kind: ServiceEntry\n" +
                "metadata:\n" +
                "  name: static-7288\n" +
                "  namespace: gateway-system\n" +
                "spec:\n" +
                "  endpoints:\n" +
                "  - address: test.d\n" +
                "    labels:\n" +
                "      gw_cluster: gw1\n" +
                "  - address: test.f\n" +
                "    labels:\n" +
                "      gw_cluster: gw3\n" +
                "  - address: test.z\n" +
                "    labels:\n" +
                "      gw_cluster: gw4\n" +
                "  exportTo:\n" +
                "  - '*'\n" +
                "  hosts:\n" +
                "  - testhost\n" +
                "  location: MESH_EXTERNAL\n" +
                "  ports:\n" +
                "  - name: http\n" +
                "    number: 81\n" +
                "    protocol: HTTP\n" +
                "  resolution: DNS";

        ServiceEntry se1 = CommonUtil.yaml2Obj(yaml1, ServiceEntry.class);
        ServiceEntry se2 = CommonUtil.yaml2Obj(yaml2, ServiceEntry.class);

        ServiceEntry merged = operator.merge(se1, se2);

        ServiceEntrySpec spec = merged.getSpec();
        Assert.assertEquals(new Integer(81), spec.getPorts().get(0).getNumber());
        Assert.assertEquals(4, spec.getEndpoints().size());

        List<Endpoint> gw1 = filterByGwLabel(spec.getEndpoints(), "gw1");

        Assert.assertEquals(1, gw1.size());
        Assert.assertEquals("test.d", gw1.get(0).getAddress());

        List<Endpoint> gw2 = filterByGwLabel(spec.getEndpoints(), "gw2");

        Assert.assertEquals(1, gw2.size());
        Assert.assertEquals("test.c", gw2.get(0).getAddress());

        List<Endpoint> gw3 = filterByGwLabel(spec.getEndpoints(), "gw3");

        Assert.assertEquals(1, gw3.size());
        Assert.assertEquals("test.f", gw3.get(0).getAddress());

        List<Endpoint> gw4 = filterByGwLabel(spec.getEndpoints(), "gw4");

        Assert.assertEquals(1, gw4.size());
        Assert.assertEquals("test.z", gw4.get(0).getAddress());
    }


    private List<Endpoint> filterByGwLabel(List<Endpoint> endpoints, String gateway) {
        return endpoints.stream()
                .filter(e -> e.getLabels().equals(buildGatewayLabel(gateway)))
                .collect(Collectors.toList());
    }

    private Map<String, String> buildGatewayLabel(String gateway) {
        return ImmutableMap.of("gw_cluster",  gateway);
    }

}
