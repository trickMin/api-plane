package com.netease.cloud.nsf.core.operator;


import com.netease.cloud.nsf.core.istio.operator.GatewayOperator;
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

        List<String> oldHosts = Arrays.asList("a", "b");
        List<String> freshHosts = Arrays.asList("b", "c", "d");

        Gateway old = new Gateway();
        GatewaySpec oldSpec = new GatewaySpec();
        Server oldServer = new Server();
        oldServer.setHosts(oldHosts);
        oldSpec.setServers(Arrays.asList(oldServer));
        old.setSpec(oldSpec);

        Gateway fresh = new Gateway();
        GatewaySpec freshSpec = new GatewaySpec();
        Server freshServer = new Server();
        freshServer.setHosts(freshHosts);
        freshSpec.setServers(Arrays.asList(freshServer));
        fresh.setSpec(freshSpec);

        Gateway merge = operator.merge(old, fresh);
        Assert.assertTrue(merge.getSpec().getServers().get(0).getHosts().size() == 4);

    }
}
