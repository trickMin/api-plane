package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.Gateway;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/14
 **/
public class GatewayPluginSharedConfigDataHandler extends GatewayPluginDataHandler {

    private String sharedConfigName;
    private String configMapNamespace;

    public GatewayPluginSharedConfigDataHandler(List<FragmentWrapper> fragments, List<Gateway> gateways, String sharedConfigName, String configMapNamespace, String gatewayNamespace) {
        super(fragments, gateways, gatewayNamespace);
        this.sharedConfigName = sharedConfigName;
        this.configMapNamespace = configMapNamespace;
    }

    @Override
    List<TemplateParams> doHandle(TemplateParams baseParams) {
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

        if (CollectionUtils.isEmpty(descriptors)) return Collections.EMPTY_LIST;
        return Arrays.asList(TemplateParams.instance()
                .setParent(baseParams)
                .put(SHARED_CONFIG_NAME, sharedConfigName)
                .put(NAMESPACE, configMapNamespace)
                .put(SHARED_CONFIG_DESCRIPTOR, descriptors));

    }
}
