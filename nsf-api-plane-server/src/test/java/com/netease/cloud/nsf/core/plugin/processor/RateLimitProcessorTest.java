package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import org.junit.Assert;
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

        String plugin3 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"pre_condition\": [\n" +
                "    {\n" +
                "      \"custom_extractor\": \"Header[plugin1]\",\n" +
                "      \"operator\": \"present\",\n" +
                "      \"invert\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"custom_extractor\": \"Header[plugin2]\",\n" +
                "      \"operator\": \"=\",\n" +
                "      \"right_value\": \"ratelimit\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"hour\": 1\n" +
                "  }\n" +
                "  ]\n" +
                "}";
        String plugin4 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "  {\n" +
                "    \"pre_condition\": [\n" +
                "    {\n" +
                "      \"custom_extractor\": \"Header[plugin1]\",\n" +
                "      \"operator\": \"present\",\n" +
                "      \"invert\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"custom_extractor\": \"Header[plugin2]\",\n" +
                "      \"operator\": \"=\",\n" +
                "      \"right_value\": \"ratelimit\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"hour\": 1,\n" +
                "    \"type\": \"Local\"\n" +
                "  }\n" +
                "  ]\n" +
                "}";

        FragmentHolder fragment1 = processor.process(plugin1, serviceInfo);
        FragmentHolder fragment2 = processor.process(plugin2, serviceInfo);
        FragmentHolder fragment3 = processor.process(plugin3, serviceInfo);
        FragmentHolder fragment4 = processor.process(plugin4, serviceInfo);
        FragmentHolder fragment5 = processor.process(plugin4, nullInfo);
        //TODO assert
    }

    @Test
    public void hash() {
        String plugin1 = "{\n" +
                "  \"kind\": \"ianus-rate-limiting\",\n" +
                "  \"limit_by_list\": [\n" +
                "    {\n" +
                "      \"pre_condition\": [\n" +
                "        {\n" +
                "          \"custom_extractor\": \"Header[plugin1]\",\n" +
                "          \"operator\": \"present\",\n" +
                "          \"invert\": true\n" +
                "        },\n" +
                "        {\n" +
                "          \"custom_extractor\": \"Header[plugin2]\",\n" +
                "          \"operator\": \"=\",\n" +
                "          \"right_value\": \"ratelimit\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"hour\": 1,\n" +
                "      \"second\": 1\n" +
                "    }\n" +
                "  ]\n" +
                "}";


        FragmentHolder fragment1 = processor.process(plugin1, serviceInfo);
        FragmentHolder fragment2 = processor.process(plugin1, serviceInfo);
        Assert.assertEquals(fragment1.getSharedConfigFragment().getContent(),fragment2.getSharedConfigFragment().getContent());
    }
}