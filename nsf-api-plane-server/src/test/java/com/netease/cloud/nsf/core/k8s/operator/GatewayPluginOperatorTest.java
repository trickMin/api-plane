package com.netease.cloud.nsf.core.k8s.operator;

import com.netease.cloud.nsf.core.k8s.operator.GatewayPluginOperator;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPluginSpec;
import me.snowdrop.istio.api.networking.v1alpha3.Plugins;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GatewayPluginOperatorTest {

    GatewayPluginOperator operator;

    @Before
    public void init() {
        operator = new GatewayPluginOperator();
    }

    @Test
    public void merge() {

        GatewayPlugin gp1 = getGatewayPlugin(getGatewayPluginSpec(Arrays.asList("gw-1", "gw-2"),
                Arrays.asList("host1", "host2"),
                Arrays.asList(getPlugins("p1", Collections.emptyMap())),
                Arrays.asList("route1", "route2"),
                Arrays.asList("service1", "service2"),
                Arrays.asList("user1")));

        GatewayPlugin gp2 = getGatewayPlugin(getGatewayPluginSpec(Arrays.asList("gw-3"),
                Arrays.asList("host3", "host4"),
                Arrays.asList(getPlugins("p2", Collections.emptyMap())),
                Arrays.asList("route3"),
                Arrays.asList("service3"),
                Collections.emptyList()));

        GatewayPlugin merge = operator.merge(gp1, gp2);

        GatewayPluginSpec spec = merge.getSpec();
        assertEquals(1, spec.getGateway().size());
        assertEquals("gw-3", spec.getGateway().get(0));
        assertEquals(2, spec.getHost().size());
        assertTrue(spec.getHost().contains("host3") && spec.getHost().contains("host4"));
        assertEquals(1, spec.getPlugins().size());
        assertEquals("p2", spec.getPlugins().get(0).getName());
        assertEquals(1, spec.getRoute().size());
        assertEquals("route3", spec.getRoute().get(0));
        assertEquals("service3", spec.getService().get(0));
        assertTrue(CollectionUtils.isEmpty(spec.getUser()));
    }


    private static GatewayPlugin getGatewayPlugin(GatewayPluginSpec spec) {
        GatewayPlugin gp = new GatewayPlugin();
        gp.setSpec(spec);
        return gp;
    }

    private static GatewayPluginSpec getGatewayPluginSpec(List<String> gateway, List<String> hosts, List<Plugins> plugins,
                                                          List<String> routes, List<String> service, List<String> user) {
        GatewayPluginSpec spec = new GatewayPluginSpec();
        spec.setGateway(gateway);
        spec.setHost(hosts);
        spec.setPlugins(plugins);
        spec.setRoute(routes);
        spec.setService(service);
        spec.setUser(user);
        return spec;
    }

    private static Plugins getPlugins(String name, Map<String, Object> settings) {
        Plugins plugins = new Plugins();
        plugins.setName(name);
        plugins.setSettings(settings);
        return plugins;
    }
}
