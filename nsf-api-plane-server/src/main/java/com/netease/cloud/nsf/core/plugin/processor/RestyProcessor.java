package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RestyProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "RestyProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        ResourceGenerator builder = ResourceGenerator.newInstance("{}", ResourceType.JSON);
        String kind = source.getValue("$.kind", String.class);
        Object config = source.getValue("$.config");
        builder.createOrUpdateValue("$", "config", config);
        builder.createOrUpdateValue("$", "name", kind);

        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(source))
                .withContent(builder.yamlString())
                .withResourceType(K8sResourceEnum.VirtualService)
                .withFragmentType(FragmentTypeEnum.VS_API)
                .build();
        holder.setVirtualServiceFragment(wrapper);
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

        ResourceGenerator builder = ResourceGenerator.newInstance("{\"plugins\":[]}");
        holders.forEach(item -> {
            builder.addElement("$.plugins",
            ResourceGenerator.newInstance(item.getVirtualServiceFragment().getContent(), ResourceType.YAML).getValue("$"));
        });


        List<FragmentHolder> ret = new ArrayList<>();
        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withContent(builder.yamlString())
                .withResourceType(K8sResourceEnum.VirtualService)
                .withFragmentType(FragmentTypeEnum.VS_API)
                .build();
        holder.setVirtualServiceFragment(wrapper);
        coverToExtensionPlugin(holder, "com.netease.resty");
        ret.add(holder);
        return ret;
    }
}
