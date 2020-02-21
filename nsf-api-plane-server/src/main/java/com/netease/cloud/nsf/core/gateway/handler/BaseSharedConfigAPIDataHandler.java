package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.API;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.SHARED_CONFIG_DESCRIPTOR;
import static com.netease.cloud.nsf.core.template.TemplateConst.SHARED_CONFIG_NAME;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class BaseSharedConfigAPIDataHandler extends APIDataHandler {

    private String sharedConfigName;
    private List<FragmentWrapper> fragments;

    public BaseSharedConfigAPIDataHandler(List<FragmentWrapper> fragments, String sharedConfigName) {
        this.fragments = fragments;
        this.sharedConfigName = sharedConfigName;
    }

    @Override
    List<TemplateParams> doHandle(TemplateParams baseParams, API api) {
        if (CollectionUtils.isEmpty(fragments)) return Collections.emptyList();

        List<String> descriptors = fragments.stream()
                .filter(f -> f != null)
                // 因配置迁移，模型有变，不再为数组
                .map(f -> {
                    String content = f.getContent();
                    if (content.startsWith("-")) content = content.replaceFirst("-", " ");
                    return content;
                })
                .collect(Collectors.toList());

        return Arrays.asList(TemplateParams.instance()
                                .setParent(baseParams)
                                .put(SHARED_CONFIG_NAME, sharedConfigName)
                                .put(SHARED_CONFIG_DESCRIPTOR, descriptors));
    }
}
