package com.netease.cloud.nsf.core.k8s.subtracter;

import com.google.common.collect.ImmutableMap;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRuleSpec;
import me.snowdrop.istio.api.networking.v1alpha3.Subset;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class GatewayDestinationRuleSubtracterTest {

    @Test
    public void testSubtract() {

        GatewayDestinationRuleSubtracter subtracter1 = new GatewayDestinationRuleSubtracter("se-s1");

        Subset s1 = getSubset("s1", "se-s1", null, "gw1");
        Subset s2 = getSubset("s2", "se-s2", null, "gw2");

        DestinationRuleSpec spec = getDestinationRuleSpec(Arrays.asList(s1,s2));
        DestinationRule old = getDestinationRule("v1", "destinationRule", spec);

        DestinationRule result = subtracter1.subtract(old);
        Assert.assertTrue(result.getSpec().getSubsets().size() == 1);
        Assert.assertTrue(result.getSpec().getSubsets().get(0).getName().equals("s2"));
        Assert.assertTrue(result.getSpec().getSubsets().get(0).getGwLabels().equals(getGwLabels("gw2")));
    }

    private static DestinationRule getDestinationRule(String apiVersion, String kind, DestinationRuleSpec spec) {
        DestinationRule ds = new DestinationRule();
        ds.setApiVersion(apiVersion);
        ds.setKind(kind);
        ds.setSpec(spec);
        return ds;
    }

    private static Subset getSubset(String name, String api, Map<String, String> labels, String gw) {
        Subset ss = new Subset();
        ss.setName(name);
        ss.setApi(api);
        ss.setLabels(labels);
        ss.setGwLabels(getGwLabels(gw));
        return ss;
    }

    private static DestinationRuleSpec getDestinationRuleSpec(List<Subset> subsets) {
        DestinationRuleSpec drs = new DestinationRuleSpec();
        drs.setSubsets(subsets);
        return drs;
    }

    private static Map<String, String> getGwLabels(String gw) {
        return ImmutableMap.of("gw_cluster", gw);
    }
}
