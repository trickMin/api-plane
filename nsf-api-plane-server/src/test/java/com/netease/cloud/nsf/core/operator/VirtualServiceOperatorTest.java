package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.core.istio.operator.VirtualServiceOperator;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                , null));

        VirtualService fresh = getVirtualService(getVirtualServiceSpec(
                Arrays.asList(
                        getHTTPRoute("a",
                                Arrays.asList(getHTTPMatchRequest(), getHTTPMatchRequest())),
                        getHTTPRoute("b",null),
                        getHTTPRoute("c", null),
                        getHTTPRoute("c", null))
                ,null));

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
                , getPlugins(Arrays.asList("a", "b"))));

        VirtualService result = operator.subtract(old, "a");
        Assert.assertTrue(result.getSpec().getHttp().size() == 2);
        Assert.assertTrue(result.getSpec().getPlugins().size() == 1);
        result = operator.subtract(result, "b");
        Assert.assertTrue(result.getSpec().getHttp().size() == 1);
        Assert.assertTrue(result.getSpec().getPlugins().size() == 0);
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

    private static VirtualServiceSpec getVirtualServiceSpec(List<HTTPRoute> routes, Map<String, ApiPlugin> plugins) {
        VirtualServiceSpec spec = new VirtualServiceSpec();
        spec.setHttp(routes);
        spec.setPlugins(plugins);
        return spec;
    }

    private static VirtualService getVirtualService(VirtualServiceSpec spec) {
        VirtualService vs = new VirtualService();
        vs.setSpec(spec);
        return vs;
    }

    private static Map<String, ApiPlugin> getPlugins(List<String> apis) {
        Map<String, ApiPlugin> pluginMap = new HashMap<>();
        apis.forEach(a -> pluginMap.put(a, new ApiPlugin()));
        return pluginMap;
    }
}
