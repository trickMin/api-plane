package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.util.CommonUtil;
import io.fabric8.kubernetes.api.model.ConfigMap;
import me.snowdrop.istio.api.networking.v1alpha3.RateLimitConfig;
import me.snowdrop.istio.api.networking.v1alpha3.RateLimitDescriptor;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RateLimitConfigMapSubtracterTest {

    @Test
    public void subtract() {

        Map<String, String> data1 = buildData("config.yaml",
                "  descriptors:\n" +
                        "  - api: auto-test1\n" +
                        "    key: header_match\n" +
                        "    rateLimit:\n" +
                        "      requestsPerUnit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: 123\n" +
                        "  - api: auto-test1\n" +
                        "    key: header_match\n" +
                        "    rateLimit:\n" +
                        "      requestsPerUnit: 1\n" +
                        "      unit: MINUTE\n" +
                        "    value: auto-test1\n" +
                        "  - api: auto-test3\n" +
                        "    key: header_match\n" +
                        "    rateLimit:\n" +
                        "      requestsPerUnit: 1\n" +
                        "      unit: HOUR\n" +
                        "    value: good\n" +
                        "  domain: qingzhou");


        RateLimitConfigMapSubtracter subtracter = new RateLimitConfigMapSubtracter("auto-test1");
        ConfigMap configMap1 = buildConfigMap(data1);
        ConfigMap subtracted = subtracter.subtract(configMap1);

        String rawData = subtracted.getData().get("config.yaml");
        RateLimitConfig rateLimitConfig = CommonUtil.yaml2Obj(rawData, RateLimitConfig.class);

        Assert.assertEquals(1, rateLimitConfig.getDescriptors().size());
        RateLimitDescriptor oneRateLimit = rateLimitConfig.getDescriptors().get(0);
        Assert.assertEquals("auto-test3", oneRateLimit.getApi());
        Assert.assertEquals("HOUR", oneRateLimit.getRateLimit().getUnit());
        Assert.assertEquals(new Integer(1), oneRateLimit.getRateLimit().getRequestsPerUnit());
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