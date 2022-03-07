package com.netease.cloud.nsf.core.k8s.operator;


import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.proto.k8s.K8sTypes;
import istio.networking.v1alpha3.DestinationRuleOuterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DestinationRuleOperatorTest {

    DestinationRuleOperator operator;


    @Before
    public void init() {
        operator = new DestinationRuleOperator();
    }


    @Test
    public void testMerge() {

        Map<String, String> labels = ImmutableMap.of("a", "b");

        DestinationRuleOuterClass.Subset s1 = getSubset("s1", null, null, "gw1");
        DestinationRuleOuterClass.Subset s2 = getSubset("s2", null, null, "gw2");
        DestinationRuleOuterClass.Subset s3 = getSubset("s3", null, null, "gw3");
        K8sTypes.DestinationRule old = new K8sTypes.DestinationRule();
        old.setApiVersion("v1");
        old.setKind("destinationRule");
        old.setSpec(DestinationRuleOuterClass.DestinationRule.newBuilder()
                .addAllSubsets(Arrays.asList(s1,s2,s3)).build());

        DestinationRuleOuterClass.Subset fresh1 = getSubset("s1", null, labels, "gw11");
        DestinationRuleOuterClass.Subset fresh4 = getSubset("s4", null, null, "gw4");
        K8sTypes.DestinationRule fresh = new K8sTypes.DestinationRule();
        old.setApiVersion("v1");
        old.setKind("destinationRule");
        old.setSpec(DestinationRuleOuterClass.DestinationRule.newBuilder()
                .addAllSubsets(Arrays.asList(fresh1,fresh4)).build());

        K8sTypes.DestinationRule destinationRule = operator.merge(old, fresh);
        List<DestinationRuleOuterClass.Subset> subsets = destinationRule.getSpec().getSubsetsList();
        Assert.assertEquals(4, subsets.size());
        subsets.forEach(ss -> {
            if (ss.getName().equals("s1")) {
                Assert.assertEquals(ss.getGwLabelsMap(), getGwLabels("gw11"));
            } else if (ss.getName().equals("s4")) {
                Assert.assertEquals(ss.getGwLabelsMap(), getGwLabels("gw4"));
            }
        });
    }

    @Test
    public void testSubtract() {

        DestinationRuleOuterClass.Subset s1 = getSubset("s1", "se-s1", null, "gw1");
        DestinationRuleOuterClass.Subset s2 = getSubset("s2", "se-s2", null, "gw2");
        K8sTypes.DestinationRule old = new K8sTypes.DestinationRule();
        old.setApiVersion("v1");
        old.setKind("destinationRule");
        old.setSpec(DestinationRuleOuterClass.DestinationRule.newBuilder()
                .addAllSubsets(Arrays.asList(s1,s2)).build());


        K8sTypes.DestinationRule result = operator.subtract(old, "se-s1");
        Assert.assertEquals(1, result.getSpec().getSubsetsList().size());
        Assert.assertEquals("s2", result.getSpec().getSubsetsList().get(0).getName());
        Assert.assertEquals(result.getSpec().getSubsetsList().get(0).getGwLabelsMap(), getGwLabels("gw2"));
    }



    private static DestinationRuleOuterClass.Subset getSubset(String name, String api, Map<String, String> labels, String gw) {
        return DestinationRuleOuterClass.Subset.newBuilder().setName(name).setApi(api).putAllLabels(labels).putAllGwLabels(getGwLabels(gw)).build();
    }


    private static Map<String, String> getGwLabels(String gw) {
        return ImmutableMap.of("gw_cluster", gw);
    }
}
