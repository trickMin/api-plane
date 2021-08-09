package com.netease.cloud.nsf.core.plugin.processor;

import com.google.common.collect.Maps;
import com.mysql.jdbc.StringUtils;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhufengwei.sx
 * @date 2021/8/6 9:56
 * 基于Http header进行黑白名单过滤的插件
 */

@Component
public class HeadRestrictionProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "HeadRestrictionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        PluginGenerator ret = PluginGenerator.newInstance("{\"headers\":[]}");
        ret.createOrUpdateJson("$.head_restriction", "type", rg.getValue("$.type", String.class));
        List<String> filterContents = rg.getValue("$.list[*]");
        for (String filterContent : filterContents) {
            if (StringUtils.isNullOrEmpty(filterContent)){
                continue;
            }
            String name = String.format("\"name\":\"%s\"", rg.getValue("$.name", String.class));
            String match = String.format("\"safe_regex_match\":\"%s\"", filterContent);
            ret.addJsonElement("$.headers", String.format("{%s,%s}", name, match));
        }
        String listType = Objects.equals("0", rg.getValue("$.type", String.class)) ? "black_list" : "white_list";
        ret.createOrUpdateJson("$", listType, ret.jsonString());
        ret.removeElement("$.headers");
        ret.createOrUpdateJson("$", "config", ret.jsonString());
        ret.removeElement(String.format("$.%s", listType));
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(rg))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(ret.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }
}

