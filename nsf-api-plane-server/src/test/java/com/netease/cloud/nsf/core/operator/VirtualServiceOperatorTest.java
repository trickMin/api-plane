package com.netease.cloud.nsf.core.operator;

import me.snowdrop.istio.api.networking.v1alpha3.HTTPMatchRequest;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceSpec;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class VirtualServiceOperatorTest {

    VirtualServiceOperator operator;

    @Before
    public void init() {
        operator = new VirtualServiceOperator();
    }

    @Test
    public void merge() {

        List<HTTPMatchRequest> twoMatches = Arrays.asList(new HTTPMatchRequest(), new HTTPMatchRequest());
        List<HTTPMatchRequest> threeMatches = Arrays.asList(new HTTPMatchRequest(), new HTTPMatchRequest());

        VirtualService old = new VirtualService();
        VirtualServiceSpec oldSpec = new VirtualServiceSpec();
        HTTPRoute oldHttpA = new HTTPRoute();
        oldHttpA.setName("a");
        HTTPRoute oldHttpB = new HTTPRoute();
        oldHttpB.setName("b");
        oldHttpB.setMatch(threeMatches);

        oldSpec.setHttp(Arrays.asList(oldHttpA, oldHttpB));
        old.setSpec(oldSpec);

        VirtualService fresh = new VirtualService();
        VirtualServiceSpec freshSpec = new VirtualServiceSpec();
        HTTPRoute freshHttpA = new HTTPRoute();
        freshHttpA.setName("a");
        freshHttpA.setMatch(twoMatches);
        HTTPRoute freshHttpB = new HTTPRoute();
        freshHttpB.setName("b");
        HTTPRoute freshHttpC = new HTTPRoute();
        freshHttpC.setName("c");

        freshSpec.setHttp(Arrays.asList(freshHttpA, freshHttpB, freshHttpC));
        fresh.setSpec(freshSpec);

        VirtualService merge = operator.merge(old, fresh);
        assertTrue(merge.getSpec().getHttp().size() == 3);

        for (HTTPRoute httpRoute : merge.getSpec().getHttp()) {
            if (httpRoute.getName().equals("a")) {
                assertTrue(httpRoute.getMatch().size() == 2);
            } else if (httpRoute.getName().equals("b")) {
                assertTrue(httpRoute.getMatch() == null || httpRoute.getMatch().size() == 0);
            }
        }

    }
}
