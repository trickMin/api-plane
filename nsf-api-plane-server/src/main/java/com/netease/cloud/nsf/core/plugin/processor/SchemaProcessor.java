package com.netease.cloud.nsf.core.plugin.processor;


import com.google.common.collect.ImmutableList;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/5
 **/
public interface SchemaProcessor<T> {
    // processor名，对应label #@processor
    String getName();

    FragmentHolder process(String plugin, T serviceInfo);

    // 默认多个插件的process只是用换行符join起来
    default List<FragmentHolder> process(List<String> plugins, T serviceInfo) {
        List<FragmentHolder> holders = plugins.stream()
                .map(plugin -> process(plugin, serviceInfo))
                .collect(Collectors.toList());

        List<FragmentWrapper> virtualServices = holders.stream()
                .map(FragmentHolder::getVirtualServiceFragment)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        FragmentHolder combineHolder = new FragmentHolder();
        if (!CollectionUtils.isEmpty(virtualServices)) {
            List<String> contents = virtualServices.stream().map(FragmentWrapper::getContent).collect(Collectors.toList());
            FragmentWrapper wrapper = new FragmentWrapper.Builder()
                    .withContent(String.join("\n", contents))
                    .withResourceType(virtualServices.get(0).getResourceType())
                    .withFragmentType(virtualServices.get(0).getFragmentType())
                    .build();
            combineHolder.setVirtualServiceFragment(wrapper);
        }

        return ImmutableList.of(combineHolder);
    }
}
