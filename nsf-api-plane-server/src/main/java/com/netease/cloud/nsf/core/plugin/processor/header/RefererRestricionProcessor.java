package com.netease.cloud.nsf.core.plugin.processor.header;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.core.plugin.processor.AbstractSchemaProcessor;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RefererRestricionProcessor extends HeaderRestrictionProcessor {


    @Override
    public String getName() {
        return "RefererRestricionProcessor";
    }


    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        setPluginHeader("Referer");
        return super.process(plugin, serviceInfo);
    }

    public static void main(String[] args) {
        RefererRestricionProcessor processor = new RefererRestricionProcessor();
        String str = "{\n" +
                "    \"list\": [\n" +
                "        {\n" +
                "            \"match_type\": \"exact_match\",\n" +
                "            \"value\": [\n" +
                "                \"test\",\n" +
                "                \"tt\"\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"match_type\": \"prefix_match\",\n" +
                "            \"value\": [\n" +
                "                \"www.baidu.com\",\n" +
                "                \"www.google.com\"\n" +
                "            ]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"type\": \"0\",\n" +
                "    \"kind\": \"header-restriction\"\n" +
                "}";
        processor.process(str,null);
    }

}
