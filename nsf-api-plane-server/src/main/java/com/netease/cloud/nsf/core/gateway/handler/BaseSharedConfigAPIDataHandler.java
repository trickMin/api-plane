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

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class BaseSharedConfigAPIDataHandler extends APIDataHandler {

    private List<FragmentWrapper> fragments;

    public BaseSharedConfigAPIDataHandler(List<FragmentWrapper> fragments) {
        this.fragments = fragments;
    }


    @Override
    List<TemplateParams> doHandle(TemplateParams baseParams, API api) {
        if (CollectionUtils.isEmpty(fragments)) return Collections.emptyList();

        List<String> descriptors = fragments.stream()
                .filter(f -> f != null)
                .map(f -> f.getContent())
                .collect(Collectors.toList());

        return Arrays.asList(TemplateParams.instance()
                                .setParent(baseParams)
                                .put(SHARED_CONFIG_DESCRIPTOR, descriptors));
    }
}
