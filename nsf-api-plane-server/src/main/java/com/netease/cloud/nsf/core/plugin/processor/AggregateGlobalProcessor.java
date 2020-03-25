package com.netease.cloud.nsf.core.plugin.processor;

import com.google.common.collect.Lists;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.CommonUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public List<FragmentHolder> process(List<String> plugins, ServiceInfo serviceInfo) {
        List<FragmentHolder> ret = Lists.newArrayList();

        List<String> luaPlugins = plugins.stream().filter(CommonUtil::isLuaPlugin).collect(Collectors.toList());
        List<FragmentHolder> luaHolder = getProcessor("RestyProcessor").process(luaPlugins, serviceInfo);
        luaHolder.forEach(this::convertToGatewayPlugin);
        ret.addAll(luaHolder);

        List<String> notLuaPlugins = plugins.stream().filter(item -> !CommonUtil.isLuaPlugin(item)).collect(Collectors.toList());

        List<FragmentHolder> notLuaHolder = notLuaPlugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());
        ret.addAll(notLuaHolder);

        return ret;
    }
}
