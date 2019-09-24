package com.netease.cloud.nsf.core.operator;


import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.core.istio.operator.DestinationRuleOperator;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRuleSpec;
import me.snowdrop.istio.api.networking.v1alpha3.Subset;
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

        DestinationRule old = new DestinationRule();
        old.setApiVersion("v1");
        old.setKind("destinationRule");
        old.setMetadata(new ObjectMeta());

        DestinationRuleSpec spec = new DestinationRuleSpec();
        Subset s1 = new Subset();
        s1.setName("s1");
        Subset s2 = new Subset();
        s2.setName("s2");
        Subset s3 = new Subset();
        s3.setName("s3");

        spec.setSubsets(Arrays.asList(s1,s2,s3));
        old.setSpec(spec);

        DestinationRule fresh = new DestinationRule();
        fresh.setApiVersion("v1");
        fresh.setKind("destinationRule");
        fresh.setMetadata(new ObjectMeta());

        DestinationRuleSpec freshSpec = new DestinationRuleSpec();
        Subset freshs1 = new Subset();
        freshs1.setName("s1");
        freshs1.setLabels(labels);
        Subset freshs4 = new Subset();
        freshs4.setName("s4");


        freshSpec.setSubsets(Arrays.asList(freshs1, freshs4));
        fresh.setSpec(freshSpec);

        DestinationRule destinationRule = operator.merge(old, fresh);
        List<Subset> subsets = destinationRule.getSpec().getSubsets();
        Assert.assertTrue(subsets.size() == 4);
    }
}
