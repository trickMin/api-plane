package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/11/11
 **/
@Component
public class CorsProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Override
    public String getName() {
        return "CorsProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator rg = ResourceGenerator.newInstance(plugin);
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(rg.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }
}
