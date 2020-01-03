package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.core.istio.operator.VirtualServiceOperator;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertNull;
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
                , getPlugins(Arrays.asList("a", "b"), 2)));

        VirtualService fresh = getVirtualService(getVirtualServiceSpec(
                Arrays.asList(
                        getHTTPRoute("a",
                                Arrays.asList(getHTTPMatchRequest(), getHTTPMatchRequest())),
                        getHTTPRoute("b",null),
                        getHTTPRoute("c", null),
                        getHTTPRoute("c", null))
                , getPlugins(Arrays.asList("a", "c"), 3)));

        VirtualService fresh1 = getVirtualService(getVirtualServiceSpec(
                Arrays.asList(
                        getHTTPRoute("a",
                                Arrays.asList(getHTTPMatchRequest(), getHTTPMatchRequest())),
                        getHTTPRoute("b",null),
                        getHTTPRoute("c", null),
                        getHTTPRoute("c", null))
                , Collections.EMPTY_MAP));

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
        assertTrue(merge.getSpec().getPlugins().size() == 3);

        Map<String, ApiPlugins> pluginMap = merge.getSpec().getPlugins();
        assertTrue(pluginMap.get("a").getUserPlugin().size() == 3);

        VirtualService merge1 = operator.merge(old, fresh1);
        Map<String, ApiPlugins> pluginMap1 = merge1.getSpec().getPlugins();
        assertNull(pluginMap1);
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
                , getPlugins(Arrays.asList("a", "b"), 2)));

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

    private static VirtualServiceSpec getVirtualServiceSpec(List<HTTPRoute> routes, Map<String, ApiPlugins> plugins) {
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

    private static Map<String, ApiPlugins> getPlugins(List<String> apis, int apiPluginsNum) {
        Map<String, ApiPlugins> pluginMap = new HashMap<>();
        apis.forEach(a -> pluginMap.put(a, getApiPlugins(apiPluginsNum)));
        return pluginMap;
    }

    private static ApiPlugins getApiPlugins(int num) {
        List<ApiPlugin> apList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            apList.add(new ApiPlugin());
        }
        ApiPlugins aps = new ApiPlugins();
        aps.setUserPlugin(apList);
        return aps;
    }
}
