package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/9/26
 **/
@Component
public class FlowLimitProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "FlowLimitProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        int percent = Integer.parseInt(rg.getValue("$.limit_percent", String.class));
        PluginGenerator ret = PluginGenerator.newInstance(String.format("{\"abort\":{\"http_status\":429,\"percentage\":{\"denominator\":\"MILLION\",\"numerator\":%s}}}", percent * 10000));

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
