package com.netease.cloud.nsf.core.plugin.processor.header;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;
@Component
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
