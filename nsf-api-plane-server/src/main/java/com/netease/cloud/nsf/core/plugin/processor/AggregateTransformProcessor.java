package com.netease.cloud.nsf.core.plugin.processor;

import com.google.common.collect.ImmutableList;
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
 * @date 2019/11/26
 **/
@Component
public class AggregateTransformProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "AggregateTransformProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator rg = ResourceGenerator.newInstance(plugin);
        switch (rg.getValue("$.kind", String.class)) {
            case "rewrite":
                return getProcessor("RewriteProcessor").process(plugin, serviceInfo);
            case "jsonp":
                return getProcessor("JsonpProcessor").process(plugin, serviceInfo);
            case "request-transformer":
            default:
                return getProcessor("DefaultProcessor").process(plugin, serviceInfo);
        }
    }

    @Override
    public List<FragmentHolder> process(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> holders = plugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());
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
            ResourceGenerator builder = ResourceGenerator.newInstance("{\"transformation\":{\"requestTransformations\":[],\"responseTransformations\":[]}}");
            for (FragmentWrapper wrapper : userMap.getValue()) {
                ResourceGenerator source = ResourceGenerator.newInstance(wrapper.getContent(), ResourceType.YAML);
                if (source.contain("$.transformation.requestTransformations")) {
                    int requestTransLen = source.getValue("$.transformation.requestTransformations.length()");
                    for (int i = 0; i < requestTransLen; i++) {
                        String path = String.format("$.transformation.requestTransformations[%s]", i);
                        builder.addElement("$.transformation.requestTransformations", source.getValue(path));
                    }
                }
                if (source.contain("$.transformation.responseTransformations")) {
                    int requestTransLen = source.getValue("$.transformation.responseTransformations.length()");
                    for (int i = 0; i < requestTransLen; i++) {
                        String path = String.format("$.transformation.responseTransformations[%s]", i);
                        builder.addElement("$.transformation.responseTransformations", source.getValue(path));
                    }
                }
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
