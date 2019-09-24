package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.core.istio.operator.SharedConfigOperator;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/28
 **/
public class SharedConfigTest {

    SharedConfigOperator operator;

    @Before
    public void init() {
        operator = new SharedConfigOperator();
    }

    @Test
    public void merge() {

        SharedConfig sc = new SharedConfig();
        SharedConfigSpec spec = new SharedConfigSpec();

        RateLimitConfig config1 = new RateLimitConfig();
        RateLimitDescriptor d1 = new RateLimitDescriptor();
        d1.setApi("sa-api1");
        SharedConfigRateLimit limit1 = new SharedConfigRateLimit();
        limit1.setRequestsPerUnit(10);
        d1.setRateLimit(limit1);

        RateLimitDescriptor d2 = new RateLimitDescriptor();
        d2.setApi("sa-api2");
        d2.setRateLimit(new SharedConfigRateLimit());

        config1.setDomain("qz");
        config1.setDescriptors(Arrays.asList(d1, d2));


        RateLimitConfig config2 = new RateLimitConfig();
        RateLimitDescriptor d3 = new RateLimitDescriptor();
        d3.setApi("sb-api1");
        d3.setRateLimit(new SharedConfigRateLimit());

        config2.setDomain("qz1");
        config2.setDescriptors(Arrays.asList(d3));

        spec.setRateLimitConfigs(Arrays.asList(config1, config2));
        sc.setSpec(spec);

        SharedConfig sc1 = new SharedConfig();
        SharedConfigSpec spec1 = new SharedConfigSpec();

        RateLimitConfig config1_1 = new RateLimitConfig();
        RateLimitDescriptor d1_1 = new RateLimitDescriptor();
        d1_1.setApi("sa-api1");
        SharedConfigRateLimit limit1_1 = new SharedConfigRateLimit();
        limit1_1.setRequestsPerUnit(5);
        d1_1.setRateLimit(limit1_1);

        RateLimitDescriptor d1_2 = new RateLimitDescriptor();
        d1_2.setApi("sa-api3");
        d1_2.setRateLimit(new SharedConfigRateLimit());

        config1_1.setDomain("qz");
        config1_1.setDescriptors(Arrays.asList(d1_2,d1_1));

        RateLimitConfig config1_2 = new RateLimitConfig();
        RateLimitDescriptor d1_3 = new RateLimitDescriptor();
        d1_3.setApi("sb-api3");
        d1_3.setRateLimit(new SharedConfigRateLimit());

        config1_2.setDomain("qz1");
        config1_2.setDescriptors(Arrays.asList(d1_3));

        RateLimitConfig config1_3 = new RateLimitConfig();
        RateLimitDescriptor d1_4 = new RateLimitDescriptor();
        d1_4.setApi("sb-api3");
        d1_4.setRateLimit(new SharedConfigRateLimit());

        config1_3.setDomain("qz2");
        config1_3.setDescriptors(Arrays.asList(d1_4));

        spec1.setRateLimitConfigs(Arrays.asList(config1_1, config1_2, config1_3));
        sc1.setSpec(spec1);

        SharedConfig result = operator.merge(sc, sc1);

        Assert.assertTrue(result.getSpec().getRateLimitConfigs().size() == 3);
        for (RateLimitConfig rateLimitConfig : result.getSpec().getRateLimitConfigs()) {
            if (rateLimitConfig.getDomain().equals("qz")) {
                Assert.assertTrue(rateLimitConfig.getDescriptors().size() == 3);
            } else if (rateLimitConfig.getDomain().equals("qz1")) {
                Assert.assertTrue(rateLimitConfig.getDescriptors().size() == 2);
                for (RateLimitDescriptor rateLimitDescriptor : rateLimitConfig.getDescriptors()) {
                    if (rateLimitDescriptor.getApi().equals("sa-api1")) {
                        Assert.assertTrue(rateLimitDescriptor.getRateLimit().getRequestsPerUnit() == 5);
                    }
                }

            } else if (rateLimitConfig.getDomain().equals("qz2")) {
                Assert.assertTrue(rateLimitConfig.getDescriptors().size() == 1);
            }
        }

        SharedConfig sc3 = new SharedConfig();
        SharedConfigSpec spec3 = new SharedConfigSpec();

        RateLimitConfig config3 = new RateLimitConfig();
        RateLimitDescriptor d3_1 = new RateLimitDescriptor();
        d3_1.setApi("sa-api1");
        SharedConfigRateLimit limit3_1 = new SharedConfigRateLimit();
        limit3_1.setRequestsPerUnit(10);
        limit3_1.setUnit("MINUTE");
        d3_1.setRateLimit(limit3_1);

        RateLimitDescriptor d3_2 = new RateLimitDescriptor();
        SharedConfigRateLimit limit3_2 = new SharedConfigRateLimit();
        limit3_2.setRequestsPerUnit(5);
        limit3_2.setUnit("HOUR");
        d3_2.setApi("sa-api1");
        d3_2.setRateLimit(limit3_2);

        config3.setDomain("qz");
        config3.setDescriptors(Arrays.asList(d3_1, d3_2));

        spec3.setRateLimitConfigs(Arrays.asList(config3));
        sc3.setSpec(spec3);

        SharedConfig sc3_ = new SharedConfigBuilder(sc3).build();
        List<RateLimitDescriptor> descriptors = sc3_.getSpec().getRateLimitConfigs().get(0).getDescriptors();
        descriptors.get(0).getRateLimit().setUnit("HOUR");
        descriptors.get(0).getRateLimit().setRequestsPerUnit(1);
        descriptors.get(1).getRateLimit().setUnit("MINUTE");
        descriptors.get(1).getRateLimit().setRequestsPerUnit(100);
        RateLimitDescriptor nd3 = new RateLimitDescriptor();
        SharedConfigRateLimit nrl3 = new SharedConfigRateLimit();
        nd3.setApi("sa");
        nrl3.setUnit("OOO");
        nrl3.setRequestsPerUnit(99);
        nd3.setRateLimit(nrl3);
        descriptors.add(0, nd3);

        RateLimitConfig nc3 = new RateLimitConfig();
        nc3.setDomain("q1z");

        sc3_.getSpec().getRateLimitConfigs().add(nc3);

        SharedConfig result1 = operator.merge(sc3, sc3_);
        Assert.assertTrue(result1.getSpec().getRateLimitConfigs().size() == 2);

        result1.getSpec().getRateLimitConfigs().forEach(c -> {
            if (c.getDomain().equals("qz")) {
                Assert.assertTrue(c.getDescriptors().size() == 3);
            }
        });

    }

    @Test
    public void subtract() {

        SharedConfig sc = new SharedConfig();
        SharedConfigSpec spec = new SharedConfigSpec();

        RateLimitConfig config1 = new RateLimitConfig();
        RateLimitDescriptor d1 = new RateLimitDescriptor();
        d1.setApi("sa-api1");
        d1.setRateLimit(new SharedConfigRateLimit());

        RateLimitDescriptor d2 = new RateLimitDescriptor();
        d2.setApi("sa-api2");
        d2.setRateLimit(new SharedConfigRateLimit());

        config1.setDomain("qz");
        config1.setDescriptors(Arrays.asList(d1, d2));


        RateLimitConfig config2 = new RateLimitConfig();
        RateLimitDescriptor d3 = new RateLimitDescriptor();
        d3.setApi("sb-api1");
        d3.setRateLimit(new SharedConfigRateLimit());

        RateLimitDescriptor d4 = new RateLimitDescriptor();
        d4.setApi("sa-api1");
        d4.setRateLimit(new SharedConfigRateLimit());

        config2.setDomain("qz1");
        config2.setDescriptors(Arrays.asList(d3, d4));

        spec.setRateLimitConfigs(Arrays.asList(config1, config2));
        sc.setSpec(spec);

        SharedConfig result = operator.subtract(sc, "sa", "api1");
        Assert.assertTrue(result.getSpec().getRateLimitConfigs().get(0).getDescriptors().size() == 1);
        Assert.assertTrue(result.getSpec().getRateLimitConfigs().get(1).getDescriptors().size() == 1);
    }
}
