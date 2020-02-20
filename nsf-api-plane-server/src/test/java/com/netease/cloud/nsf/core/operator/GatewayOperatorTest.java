package com.netease.cloud.nsf.core.operator;


import com.netease.cloud.nsf.core.k8s.operator.GatewayOperator;
import me.snowdrop.istio.api.networking.v1alpha3.Gateway;
import me.snowdrop.istio.api.networking.v1alpha3.GatewaySpec;
import me.snowdrop.istio.api.networking.v1alpha3.Server;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GatewayOperatorTest {

    GatewayOperator operator;

    @Before
    public void init() {
        operator = new GatewayOperator();
    }

    @Test
    public void testMerge() {

        Gateway old = getGateway(getGatewaySpec(Arrays.asList(getServer(Arrays.asList("a", "b")))));
        Gateway fresh = getGateway(getGatewaySpec(Arrays.asList(getServer(Arrays.asList("b", "c", "d")))));

        Gateway merge = operator.merge(old, fresh);
        Assert.assertTrue(merge.getSpec().getServers().get(0).getHosts().size() == 4);
    }

    private static Server getServer(List<String> hosts) {
        Server server = new Server();
        server.setHosts(hosts);
        return server;
    }

    private static GatewaySpec getGatewaySpec(List<Server> servers) {
        GatewaySpec spec = new GatewaySpec();
        spec.setServers(servers);
        return spec;
    }

    private static Gateway getGateway(GatewaySpec spec) {
        Gateway gateway = new Gateway();
        gateway.setSpec(spec);
        return gateway;
    }
}
