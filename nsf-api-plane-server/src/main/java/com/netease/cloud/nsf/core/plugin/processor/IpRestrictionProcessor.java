package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/9/26
 **/
@Component
public class IpRestrictionProcessor extends AbstractYxSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "IpRestrictionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator rg = ResourceGenerator.newInstance(plugin);
        ResourceGenerator ret = ResourceGenerator.newInstance("{\"ipRestriction\":{\"list\":[]}}");
        ret.createOrUpdateJson("$.ipRestriction", "type", rg.getValue("$.type", String.class));
        List<String> ips = rg.getValue("$.list[*]");
        for (String ip : ips) {
            ret.addJsonElement("$.ipRestriction.list", ip);
        }
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(ret.yamlString())
                .withXUserId(getXUserId(rg))
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }
}
