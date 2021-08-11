package com.netease.cloud.nsf.core.plugin.processor;

import com.google.common.collect.Maps;
import com.mysql.jdbc.StringUtils;
import com.netease.cloud.nsf.core.IstioModelEngine;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.meta.ServiceInfo;
import jdk.nashorn.api.scripting.JSObject;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author zhufengwei.sx
 * @date 2021/8/6 9:56
 * 基于Http header进行黑白名单过滤的插件
 */

@Component
public class HeaderRestrictionProcessor extends AbstractSchemaProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HeaderRestrictionProcessor.class);

    @Override
    public String getName() {
        return "HeaderRestrictionProcessor";
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        String yamlString = doProcess(rg);
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(rg))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(yamlString)
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }

    private String doProcess(PluginGenerator rg){
        PluginGenerator ret = PluginGenerator.newInstance("{\"config\":[]}");
        try {
            List<JSONObject> whiteConfigs = createConfig(rg, "1");
            List<JSONObject> blackConfigs = createConfig(rg, "0");
            buildConfig(ret, blackConfigs, "black_list");
            buildConfig(ret, whiteConfigs, "white_list");
        }catch (Exception e){
            logger.error("HeaderRestrictionProcessor process error\n" + e.getMessage());
            throw e;
        }

        return ret.yamlString();
    }

    private void buildConfig(PluginGenerator ret, List<JSONObject> configs, String type){
        if (configs.isEmpty()){
            return;
        }
        PluginGenerator builder = PluginGenerator.newInstance("{\"headers\":[]}");
        for (JSONObject config : configs){
            String name = String.format("\"name\":\"%s\"", config.get("name"));
            String match = String.format("\"regex\":\"%s\"", config.get("list"));
            builder.addJsonElement("$.headers", String.format("{%s,%s}", name, match));
        }
        builder.createOrUpdateJson("$", type, builder.jsonString());
        builder.removeElement("$.headers");
        ret.addJsonElement("$.config", builder.jsonString());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<JSONObject> createConfig(PluginGenerator rg, String selectType){
        List<Map> configs = rg.getValue("$.config", List.class);
        List<JSONObject> configList = new ArrayList<>();
        for (Map config : configs){
            String type = (String) config.get("type");
            if(!selectType.equals(type)){
                continue;
            }
            List<String> lists = (List)config.get("lists");
            String name = (String) config.get("name");

            for (String list : lists){
                JSONObject object = new JSONObject();
                object.put("name", name);
                object.put("list", list);
                configList.add(object);
            }
        }
        return configList;
    }
}

