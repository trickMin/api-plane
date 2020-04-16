package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/16
 **/
public class MeshRateLimitProcessorTest extends BasePluginTest {
    @Autowired
    MeshRateLimitProcessor processor;

    @Test
    public void processor() {
        String plugin1 = "{\n" +
                "  \"kind\": \"mesh-rate-limiting\",\n" +
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
                "      \"second\": 2,\n" +
                "      \"minute\": 3,\n" +
                "      \"day\": 4,\n" +
                "      \"when\": \"true\",\n" +
                "      \"then\": \"@/{pod}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        FragmentHolder fragment1 = processor.process(plugin1, serviceInfo);
    }
}
