package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class DynamicDowngradeProcessorTest extends BasePluginTest {

    @Autowired
    DynamicDowngradeProcessor dynamicDowngradeProcessor;

    @Test
    public void process() {

        String p1 = "{\n" +
                "\t\"condition\": {\n" +
                "\t\t\"request\": {\n" +
                "\t\t\t\"requestSwitch\": true,\n" +
                "\t\t\t\"path\": {\n" +
                "\t\t\t\t\"match_type\": \"safe_regex_match\",\n" +
                "\t\t\t\t\"value\": \"/anything/anythin.\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"host\": {\n" +
                "\t\t\t\t\"match_type\": \"safe_regex_match\",\n" +
                "\t\t\t\t\"value\": \"103.196.65.17.\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"headers\": [{\n" +
                "\t\t\t\t\"headerKey\": \"key\",\n" +
                "\t\t\t\t\"match_type\": \"exact_match\",\n" +
                "\t\t\t\t\"value\": \"va\"\n" +
                "\t\t\t}],\n" +
                "\t\t\t\"method\": [\n" +
                "\t\t\t\t\"GET\"\n" +
                "\t\t\t]\n" +
                "\t\t},\n" +
                "\t\t\"response\": {\n" +
                "\t\t\t\"code\": {\n" +
                "\t\t\t\t\"match_type\": \"exact_match\",\n" +
                "\t\t\t\t\"value\": \"200\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"headers\": []\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"kind\": \"dynamic-downgrade\",\n" +
                "\t\"cache\": {\n" +
                "\t\t\"condition\": {\n" +
                "\t\t\t\"response\": {\n" +
                "\t\t\t\t\"code\": {\n" +
                "\t\t\t\t\t\"match_type\": \"safe_regex_match\",\n" +
                "\t\t\t\t\t\"value\": \"2..\"\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"headers\": [{\n" +
                "\t\t\t\t\t\"headerKey\": \"x-can-downgrade\",\n" +
                "\t\t\t\t\t\"match_type\": \"exact_match\",\n" +
                "\t\t\t\t\t\"value\": \"true\"\n" +
                "\t\t\t\t}]\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\"ttls\": {\n" +
                "\t\t\t\"default\": 30000,\n" +
                "\t\t\t\"custom\": [{\n" +
                "\t\t\t\t\"code\": \"200\",\n" +
                "\t\t\t\t\"ttl\": 50000\n" +
                "\t\t\t}]\n" +
                "\t\t},\n" +
                "\t\t\"cache_key\": {\n" +
                "\t\t\t\"query_params\": [\"id\"],\n" +
                "\t\t\t\"headers\": [\"comefrom\"]\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        String p2 = "{\n" +
                "  \"condition\": {\n" +
                "    \"request\": {\n" +
                "      \"requestSwitch\": true,\n" +
                "      \"path\": {\n" +
                "        \"match_type\": \"safe_regex_match\",\n" +
                "        \"value\": \"/anything/anythin.\"\n" +
                "      },\n" +
                "      \"host\": {\n" +
                "        \"match_type\": \"safe_regex_match\",\n" +
                "        \"value\": \"103.196.65.17.\"\n" +
                "      },\n" +
                "      \"headers\": [\n" +
                "        {\n" +
                "          \"headerKey\": \"key\",\n" +
                "          \"match_type\": \"exact_match\",\n" +
                "          \"value\": \"va\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"method\": [\n" +
                "        \"GET\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"response\": {\n" +
                "      \"code\": {\n" +
                "        \"match_type\": \"exact_match\",\n" +
                "        \"value\": \"200\"\n" +
                "      },\n" +
                "      \"headers\": []\n" +
                "    }\n" +
                "  },\n" +
                "  \"kind\": \"dynamic-downgrade\",\n" +
                "  \"httpx\":{\n" +
                "    \"uri\":\"http://httpbin.org/anything\"\n" +
                "  }\n" +
                "}";

        FragmentHolder f1 = dynamicDowngradeProcessor.process(p1, serviceInfo);
        FragmentHolder f2 = dynamicDowngradeProcessor.process(p2, serviceInfo);
    }
}