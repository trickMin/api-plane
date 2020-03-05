package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RateLimitProcessorTest extends BasePluginTest {

    @Autowired
    RateLimitProcessor processor;

    @Test
    public void process() {

        String plugin1 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"identifier_extractor\": \"Header[plugin]\",\n" +
                "    \"pre_condition\": [\n" +
                "    {\n" +
                "      \"operator\": \"=\",\n" +
                "      \"right_value\": \"ratelimit\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"second\": 5,\n" +
                "    \"hour\": 10\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        String plugin2 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"identifier_extractor\": \"Header[plugin]\",\n" +
                "    \"pre_condition\": [\n" +
                "    {\n" +
                "      \"operator\": \"present\",\n" +
                "      \"invert\": true\n" +
                "    }\n" +
                "    ],\n" +
                "    \"hour\": 1\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        FragmentHolder fragment1 = processor.process(plugin1, serviceInfo);
        FragmentHolder fragment2 = processor.process(plugin2, serviceInfo);
        //TODO assert
    }
}