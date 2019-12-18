package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/12/16
 **/
@Component
public class AggregateExtensionProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Override
    public String getName() {
        return "AggregateExtensionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator rg = ResourceGenerator.newInstance(plugin);
        FragmentHolder holder;
        switch (rg.getValue("$.kind", String.class)) {
            case "rewrite":
                holder = getProcessor("RewriteProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.rewrite");
                break;
            case "jsonp":
                holder = getProcessor("JsonpProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.jsonp");
                break;
            case "transformer":
                holder = getProcessor("TransformProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.transformation");
                break;
            case "request-transformer":
            default:
                holder = getProcessor("DefaultProcessor").process(plugin, serviceInfo);
                coverToExtensionPlugin(holder, "com.netease.transformation");
                break;
        }
        return holder;
    }

    private void coverToExtensionPlugin(FragmentHolder holder, String name) {
        if (Objects.nonNull(holder.getVirtualServiceFragment())) {
            ResourceGenerator source = ResourceGenerator.newInstance(holder.getVirtualServiceFragment().getContent(), ResourceType.YAML);
            ResourceGenerator builder = ResourceGenerator.newInstance(String.format("{\"name\":\"%s\"}", name));
            builder.createOrUpdateJson("$", "settings", source.jsonString());
            holder.getVirtualServiceFragment().setContent(builder.yamlString());
        }
    }

    @Override
    public List<FragmentHolder> process(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> holders = plugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());
        // 根据租户将插件分类
        MultiValueMap<String, FragmentWrapper> xUserMap = new LinkedMultiValueMap<>();
        holders.forEach(holder -> {
            FragmentWrapper wrapper = holder.getVirtualServiceFragment();
            if (wrapper == null) return;
            String xUserId = wrapper.getXUserId();
            if (StringUtils.isEmpty(xUserId)) {
                xUserMap.add("NoneUser", wrapper);
            } else {
                xUserMap.add(xUserId, wrapper);
            }
        });
        List<FragmentHolder> ret = new ArrayList<>();
        for (Map.Entry<String, List<FragmentWrapper>> userMap : xUserMap.entrySet()) {
            ResourceGenerator builder = ResourceGenerator.newInstance("{\"ext\":[]}");
            for (FragmentWrapper wrapper : userMap.getValue()) {
                ResourceGenerator source = ResourceGenerator.newInstance(wrapper.getContent(), ResourceType.YAML);
                builder.addJsonElement("$.ext", source.jsonString());
            }
            String xUserId = "NoneUser".equals(userMap.getKey()) ? null : userMap.getKey();
            FragmentHolder holder = new FragmentHolder();
            FragmentWrapper wrapper = new FragmentWrapper.Builder()
                    .withContent(builder.yamlString())
                    .withResourceType(K8sResourceEnum.VirtualService)
                    .withFragmentType(FragmentTypeEnum.VS_API)
                    .withXUserId(xUserId)
                    .build();
            holder.setVirtualServiceFragment(wrapper);
            ret.add(holder);
        }
        return ret;
    }
}