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

import java.util.List;
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
        //todo: xUser
        List<FragmentHolder> holders = plugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"transformation\":{\"requestTransformations\":[],\"responseTransformations\":[]}}");
        for (FragmentHolder holder : holders) {
            if (Objects.isNull(holder.getVirtualServiceFragment())) continue;
            ResourceGenerator source = ResourceGenerator.newInstance(holder.getVirtualServiceFragment().getContent(), ResourceType.YAML);
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

        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withContent(builder.yamlString())
                .withResourceType(K8sResourceEnum.VirtualService)
                .withFragmentType(FragmentTypeEnum.VS_API)
                .build();
        holder.setVirtualServiceFragment(wrapper);
        return ImmutableList.of(holder);
    }
}
