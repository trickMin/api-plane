package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
        FragmentHolder holder = getProcessor("AggregateExtensionProcessor").process(plugin, serviceInfo);
        convertToGatewayPlugin(holder);
        return holder;
    }

    private void convertToGatewayPlugin(FragmentHolder holder) {
        if (Objects.nonNull(holder.getVirtualServiceFragment())) {
            holder.setGatewayPluginsFragment(holder.getVirtualServiceFragment());
            return;
        }
        if (Objects.nonNull(holder.getSharedConfigFragment())) {
            holder.setGatewayPluginsFragment(holder.getSharedConfigFragment());
            return;
        }
    }

}
