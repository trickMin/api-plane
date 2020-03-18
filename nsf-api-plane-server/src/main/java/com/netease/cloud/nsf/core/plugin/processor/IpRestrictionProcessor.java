package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/9/26
 **/
@Component
public class IpRestrictionProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "IpRestrictionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        PluginGenerator ret = PluginGenerator.newInstance("{\"list\":[]}");
        if (Objects.equals("0", rg.getValue("$.type", String.class))) {
            ret.createOrUpdateJson("$", "type", "BLACK");
        } else if (Objects.equals("1", rg.getValue("$.type", String.class))) {
            ret.createOrUpdateJson("$", "type", "WHITE");
        }
        ret.createOrUpdateJson("$.ip_restriction", "type", rg.getValue("$.type", String.class));
        List<String> ips = rg.getValue("$.list[*]");
        for (String ip : ips) {
            ret.addJsonElement("$.list", ip);
        }
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
