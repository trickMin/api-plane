package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.Gateway;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.netease.cloud.nsf.core.template.TemplateConst.SHARED_CONFIG_DESCRIPTOR;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/14
 **/
public class GatewayPluginSharedConfigDataHandler extends GatewayPluginDataHandler {


    public GatewayPluginSharedConfigDataHandler(List<FragmentWrapper> fragments, List<Gateway> gateways) {
        super(fragments, gateways);
    }

    @Override
    List<TemplateParams> doHandle(TemplateParams baseParams) {
        if (CollectionUtils.isEmpty(fragments)) return Collections.emptyList();
        List<String> descriptors = extractFragments(fragments);

        if (CollectionUtils.isEmpty(descriptors)) return Collections.EMPTY_LIST;
        return Arrays.asList(TemplateParams.instance()
                .setParent(baseParams)
                .put(SHARED_CONFIG_DESCRIPTOR, descriptors));

    }
}
