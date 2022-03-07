package com.netease.cloud.nsf.core.k8s.subtracter;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.proto.k8s.K8sTypes;
import istio.networking.v1alpha3.DestinationRuleOuterClass;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;


public class GatewayDestinationRuleSubtracterTest {

    @Test
    public void testSubtract() {

        GatewayDestinationRuleSubtracter subtracter1 = new GatewayDestinationRuleSubtracter("se-s1");

        DestinationRuleOuterClass.Subset s1 = getSubset("s1", "se-s1",  "gw1");
        DestinationRuleOuterClass.Subset s2 = getSubset("s2", "se-s2",  "gw2");
        K8sTypes.DestinationRule old = new K8sTypes.DestinationRule();
        old.setApiVersion("v1");
        old.setKind("destinationRule");
        old.setSpec(DestinationRuleOuterClass.DestinationRule.newBuilder()
                .addAllSubsets(Arrays.asList(s1,s2)).build());

        K8sTypes.DestinationRule result = subtracter1.subtract(old);
        Assert.assertEquals(1, result.getSpec().getSubsetsList().size());
        Assert.assertEquals("s2", result.getSpec().getSubsetsList().get(0).getName());
        Assert.assertEquals(result.getSpec().getSubsetsList().get(0).getGwLabelsMap(), getGwLabels("gw2"));
    }

    private static DestinationRuleOuterClass.Subset getSubset(String name, String api, String gw) {
        return DestinationRuleOuterClass.Subset.newBuilder().setName(name).setApi(api).putAllGwLabels(getGwLabels(gw)).build();
    }

    private static Map<String, String> getGwLabels(String gw) {
        return ImmutableMap.of("gw_cluster", gw);
    }
}
