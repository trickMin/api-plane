package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 静态降级插件
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2020/1/9
 **/
@Component
public class StaticDowngradeProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Override
    public String getName() {
        return "StaticDowngradeProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"static_response\":{\"headers\":[]}}");
        builder.updateValue("$.static_response.http_status", source.getValue("$.response.code", Integer.class));
        if (source.contain("$.condition")) {
            String code = source.getValue("$.condition.code.regex", String.class);
            builder.createOrUpdateJson("$", "downgrade_rpx", String.format("{\":status\":\"%s\"}", code));
        }
        if (source.contain("$.response.code")) {
            String code = source.getValue("$.response.code", String.class);
            builder.createOrUpdateJson("$.static_response", "http_status", code);
        }
        if (source.contain("$.response.body")) {
            String body = StringEscapeUtils.escapeJava(source.getValue("$.response.body", String.class));
            builder.createOrUpdateJson("$.static_response", "body", String.format("{\"inline_string\": \"%s\"}", body));
        }
        if (source.contain("$.response.header")) {
            Map<String, String> headers = source.getValue("$.response.header", Map.class);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addJsonElement("$.static_response.headers", String.format("{\"key\":\"%s\",\"value\":\"%s\"}", entry.getKey(), entry.getValue()));
            }
        }
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(source))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }
}
