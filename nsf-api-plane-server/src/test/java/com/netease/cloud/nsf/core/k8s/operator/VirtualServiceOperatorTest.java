package com.netease.cloud.nsf.core.k8s.operator;

import me.snowdrop.istio.api.networking.v1alpha3.HTTPMatchRequest;
import me.snowdrop.istio.api.networking.v1alpha3.HTTPRoute;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualServiceSpec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class VirtualServiceOperatorTest {

    VirtualServiceOperator operator;

    @Before
    public void init() {
        operator = new VirtualServiceOperator();
    }

    @Test
    public void testMerge() {

        VirtualService old = getVirtualService(getVirtualServiceSpec(
                Arrays.asList(
                    getHTTPRoute("a", null),
                    getHTTPRoute("b",
                            Arrays.asList(getHTTPMatchRequest(), getHTTPMatchRequest())),
                    getHTTPRoute("a", null))
                ));

        VirtualService fresh = getVirtualService(getVirtualServiceSpec(
                Arrays.asList(
                        getHTTPRoute("a",
                                Arrays.asList(getHTTPMatchRequest(), getHTTPMatchRequest())),
                        getHTTPRoute("b",null),
                        getHTTPRoute("c", null),
                        getHTTPRoute("c", null))
                ));

        VirtualService fresh1 = getVirtualService(getVirtualServiceSpec(
                Arrays.asList(
                        getHTTPRoute("a",
                                Arrays.asList(getHTTPMatchRequest(), getHTTPMatchRequest())),
                        getHTTPRoute("b",null),
                        getHTTPRoute("c", null),
                        getHTTPRoute("c", null)
                )));

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

    @Test
    public void testSubtract() {

        VirtualService old = getVirtualService(getVirtualServiceSpec(
                Arrays.asList(
                        getHTTPRoute("a", null),
                        getHTTPRoute("b",
                                Arrays.asList(getHTTPMatchRequest(), getHTTPMatchRequest())),
                        getHTTPRoute("a", null),
                        getHTTPRoute("c", null))
                ));

        VirtualService result = operator.subtract(old, "a");
        Assert.assertTrue(result.getSpec().getHttp().size() == 2);
        result = operator.subtract(result, "b");
        Assert.assertTrue(result.getSpec().getHttp().size() == 1);
    }

    private static HTTPRoute getHTTPRoute(String api, List<HTTPMatchRequest> requests) {
        HTTPRoute route = new HTTPRoute();
        route.setApi(api);
        route.setMatch(requests);
        return route;
    }

    private static HTTPMatchRequest getHTTPMatchRequest() {
        return new HTTPMatchRequest();
    }

    private static VirtualServiceSpec getVirtualServiceSpec(List<HTTPRoute> routes) {
        VirtualServiceSpec spec = new VirtualServiceSpec();
        spec.setHttp(routes);
        return spec;
    }

    private static VirtualService getVirtualService(VirtualServiceSpec spec) {
        VirtualService vs = new VirtualService();
        vs.setSpec(spec);
        return vs;
    }

}
