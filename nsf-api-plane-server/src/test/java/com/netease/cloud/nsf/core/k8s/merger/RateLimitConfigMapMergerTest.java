package com.netease.cloud.nsf.core.k8s.merger;

import com.netease.cloud.nsf.util.CommonUtil;
import io.fabric8.kubernetes.api.model.ConfigMap;
import me.snowdrop.istio.api.networking.v1alpha3.RateLimitConfig;
import me.snowdrop.istio.api.networking.v1alpha3.RateLimitDescriptor;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RateLimitConfigMapMergerTest {

    @Test
    public void merge() {

        RateLimitConfigMapMerger merger = new RateLimitConfigMapMerger();

        Map<String, String> data1 = buildData("config.yaml",
                "  descriptors:\n" +
                        "  - api: auto-test1\n" +
                        "    key: header_match\n" +
                        "    rateLimit:\n" +
                        "      requestsPerUnit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: 123\n" +
                        "  - api: auto-test2\n" +
                        "    key: header_match\n" +
                        "    rateLimit:\n" +
                        "      requestsPerUnit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: auto-test2\n" +
                        "  - api: auto-test3\n" +
                        "    key: header_match\n" +
                        "    rateLimit:\n" +
                        "      requestsPerUnit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: good\n" +
                        "  domain: qingzhou");

        Map<String, String> data2 = buildData("config.yaml",
                "  descriptors:\n" +
                        "  - api: auto-test1\n" +
                        "    key: header_match\n" +
                        "    rateLimit:\n" +
                        "      requestsPerUnit: 2\n" +
                        "      unit: MINUTE\n" +
                        "    value: auto-test1\n" +
                        "  - api: auto-test4\n" +
                        "    key: header_match\n" +
                        "    rateLimit:\n" +
                        "      requestsPerUnit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: auto-test4\n" +
                        "  - api: auto-test3\n" +
                        "    key: header_match\n" +
                        "    rateLimit:\n" +
                        "      requestsPerUnit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: auto-test3\n" +
                        "  domain: qingzhou\n");

        ConfigMap configMap1 = buildConfigMap(data1);
        ConfigMap configMap2 = buildConfigMap(null);
        ConfigMap configMap3 = buildConfigMap(data2);

        String s = CommonUtil.obj2yaml(configMap1);

        Assert.assertEquals(configMap1, merger.merge(configMap1, null));
        Assert.assertEquals(configMap1, merger.merge(null, configMap1));
        Assert.assertEquals(configMap1, merger.merge(configMap1, configMap2));
        Assert.assertEquals(configMap1, merger.merge(configMap2, configMap1));

        ConfigMap merge1 = merger.merge(configMap1, configMap3);
        String raw = merge1.getData().get("config.yaml");
        RateLimitConfig rlc = CommonUtil.yaml2Obj(raw, RateLimitConfig.class);

        Assert.assertEquals("qingzhou", rlc.getDomain());
        Assert.assertEquals(4, rlc.getDescriptors().size());

        for (RateLimitDescriptor rld : rlc.getDescriptors()) {
            if (rld.getApi().equals("auto-test1")) {
                Assert.assertEquals("auto-test1", rld.getValue());
                Assert.assertEquals("MINUTE", rld.getRateLimit().getUnit());
                Assert.assertEquals(new Integer(2), rld.getRateLimit().getRequestsPerUnit());
            } else if (rld.getApi().equals("auto-test2")) {
                Assert.assertEquals("HOUR", rld.getRateLimit().getUnit());
            } else if (rld.getApi().equals("auto-test4")) {
                Assert.assertEquals("auto-test4", rld.getValue());
                Assert.assertEquals("HOUR", rld.getRateLimit().getUnit());
                Assert.assertEquals(new Integer(1), rld.getRateLimit().getRequestsPerUnit());
            }
        }
    }

    private ConfigMap buildConfigMap(Map<String, String> data) {
        ConfigMap cm = new ConfigMap();
        cm.setData(data);
        return cm;
    }

    private Map<String, String> buildData(String key, String val) {
        Map<String, String> data = new HashMap<>();
        data.put(key, val);
        return data;
    }
}