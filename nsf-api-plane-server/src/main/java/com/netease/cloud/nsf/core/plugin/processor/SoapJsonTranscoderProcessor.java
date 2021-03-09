package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

@Component
public class SoapJsonTranscoderProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "SoapJsonTranscoderProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        FragmentHolder holder = new FragmentHolder();
        PluginGenerator total = PluginGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        total.removeElement("$.kind");
        holder.setVirtualServiceFragment(
                new FragmentWrapper.Builder()
                        .withXUserId(getAndDeleteXUserId(total))
                        .withFragmentType(FragmentTypeEnum.VS_API)
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withContent(total.yamlString())
                        .build()
        );
        return holder;
    }
}
