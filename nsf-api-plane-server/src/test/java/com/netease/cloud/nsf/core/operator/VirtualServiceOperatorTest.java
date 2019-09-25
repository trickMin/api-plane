package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.core.istio.operator.VirtualServiceOperator;
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
        oldHttpA.setApi("a");
        HTTPRoute oldHttpB = new HTTPRoute();
        oldHttpB.setApi("b");
        oldHttpB.setMatch(threeMatches);
        HTTPRoute oldHttpA1 = new HTTPRoute();
        oldHttpA1.setApi("a");

        oldSpec.setHttp(Arrays.asList(oldHttpA, oldHttpB, oldHttpA1));
        old.setSpec(oldSpec);

        VirtualService fresh = new VirtualService();
        VirtualServiceSpec freshSpec = new VirtualServiceSpec();
        HTTPRoute freshHttpA = new HTTPRoute();
        freshHttpA.setApi("a");
        freshHttpA.setMatch(twoMatches);
        HTTPRoute freshHttpB = new HTTPRoute();
        freshHttpB.setApi("b");
        HTTPRoute freshHttpC = new HTTPRoute();
        freshHttpC.setApi("c");
        HTTPRoute freshHttpC1 = new HTTPRoute();
        freshHttpC1.setApi("c");

        freshSpec.setHttp(Arrays.asList(freshHttpA, freshHttpB, freshHttpC, freshHttpC1));
        fresh.setSpec(freshSpec);

        VirtualService merge = operator.merge(old, fresh);
        assertTrue(merge.getSpec().getHttp().size() == 4);

        int aCount = 0;
        int cCount = 0;

        for (HTTPRoute httpRoute : merge.getSpec().getHttp()) {
            if (httpRoute.getApi().equals("a")) {
                aCount++;
                assertTrue(httpRoute.getMatch().size() == 2);
            } else if (httpRoute.getApi().equals("b")) {
                assertTrue(httpRoute.getMatch() == null || httpRoute.getMatch().size() == 0);
            } else if (httpRoute.getApi().equals("c")) {
                cCount++;
            }
        }

        assertTrue(aCount == 1);
        assertTrue(cCount == 2);
    }
}
