package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/1/17
 **/
@Component
public class AggregateGlobalProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Override
    public String getName() {
        return "AggregateGlobalProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        return getProcessor("AggregateExtensionProcessor").process(plugin, serviceInfo);
    }

}
