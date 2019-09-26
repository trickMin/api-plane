package com.netease.cloud.nsf.core.plugin;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.stereotype.Component;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/9/26
 **/
@Component
public class LuaProcessor extends AbstractYxSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "LuaProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper;
        ResourceGenerator rg = ResourceGenerator.newInstance(plugin);
        String level = rg.getValue("$.level");
        rg.removeElement("$.level");
        switch (level) {
            case "host":
                wrapper = new FragmentWrapper.Builder()
                        .withContent(rg.yamlString())
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withFragmentType(FragmentTypeEnum.VS_HOST)
                        .build();
                break;
            case "api":
                wrapper = new FragmentWrapper.Builder()
                        .withContent(rg.yamlString())
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withFragmentType(FragmentTypeEnum.VS_API)
                        .build();
                break;
            case "match":
                wrapper = new FragmentWrapper.Builder()
                        .withContent(rg.yamlString())
                        .withResourceType(K8sResourceEnum.VirtualService)
                        .withFragmentType(FragmentTypeEnum.VS_MATCH)
                        .build();
                break;
            default:
                throw new ApiPlaneException("Unsupported Lua plugin level:" + level);
        }
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }
}
