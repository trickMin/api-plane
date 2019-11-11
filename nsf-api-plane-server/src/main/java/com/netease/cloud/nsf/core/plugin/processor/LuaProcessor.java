package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/9/26
 **/
@Component
public class LuaProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "LuaProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper;
        ResourceGenerator rg = ResourceGenerator.newInstance("{\"resty\":{\"plugins\":[]}}");
        rg.addJsonElement("$.resty.plugins", plugin);
        String level = rg.getValue("$.resty.plugins[0].level");
        rg.removeElement("$.resty.plugins[0].level");
        rg.removeElement("$.resty.plugins[0].kind");
        rg.removeElement("$.resty.plugins[0].version");
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

    @Override
    public List<FragmentHolder> process(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> holders = plugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());

        List<FragmentHolder> ret = new ArrayList<>();

        List<FragmentWrapper> hostLuas = new ArrayList<>();
        List<FragmentWrapper> apiLuas = new ArrayList<>();
        List<FragmentWrapper> matchLuas = new ArrayList<>();
        Object[][] wrapperMap = new Object[][]{
                new Object[]{hostLuas, FragmentTypeEnum.VS_HOST},
                new Object[]{apiLuas, FragmentTypeEnum.VS_API},
                new Object[]{matchLuas, FragmentTypeEnum.VS_MATCH}
        };
        for (Object[] item : wrapperMap) {
            item[0] = holders.stream()
                    .filter(holder -> holder.getVirtualServiceFragment().getFragmentType().equals(item[1]))
                    .map(FragmentHolder::getVirtualServiceFragment)
                    .collect(Collectors.toList());

            List<FragmentWrapper> luas = (List<FragmentWrapper>) item[0];
            if (CollectionUtils.isEmpty(luas)) break;
            ResourceGenerator rg = ResourceGenerator.newInstance("{\"resty\":{\"plugins\":[]}}");
            luas.forEach(lua -> rg.addElement("$.resty.plugins",
                    ResourceGenerator.newInstance(lua.getContent(), ResourceType.YAML).getValue("$.resty.plugins[0]")));

            FragmentHolder holder = new FragmentHolder();
            holder.setVirtualServiceFragment(new FragmentWrapper.Builder()
                    .withContent(rg.yamlString())
                    .withResourceType(K8sResourceEnum.VirtualService)
                    .withFragmentType((FragmentTypeEnum) item[1])
                    .build());
            ret.add(holder);
        }

        return ret;
    }
}
