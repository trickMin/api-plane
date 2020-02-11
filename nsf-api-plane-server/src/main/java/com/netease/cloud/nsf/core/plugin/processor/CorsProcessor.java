package com.netease.cloud.nsf.core.plugin.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/2/11
 **/
@Component
public class CorsProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "CorsProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        ResourceGenerator builder = ResourceGenerator.newInstance("{}");
        if (source.contain("$.corsPolicy.allowOrigin")) {
            builder.createOrUpdateValue("$", "allow_origin", source.getValue("$.corsPolicy.allowOrigin", List.class));
        }
        if (source.contain("$.corsPolicy.allowOriginRegex")) {
            builder.createOrUpdateValue("$", "allow_origin_regex", source.getValue("$.corsPolicy.allowOriginRegex", List.class));
        }
        if (source.contain("$.corsPolicy.allowMethods")) {
            String allowMethods = String.join(",", source.getValue("$.corsPolicy.allowMethods", List.class));
            builder.createOrUpdateValue("$", "allow_methods", allowMethods);
        }
        if (source.contain("$.corsPolicy.allowHeaders")) {
            String allowHeaders = String.join(",", source.getValue("$.corsPolicy.allowHeaders", List.class));
            builder.createOrUpdateValue("$", "allow_headers", allowHeaders);
        }
        if (source.contain("$.corsPolicy.exposeHeaders")) {
            String exposeHeaders = String.join(",", source.getValue("$.corsPolicy.exposeHeaders", List.class));
            builder.createOrUpdateValue("$", "expose_headers", exposeHeaders);
        }
        if (source.contain("$.corsPolicy.maxAge")) {
            builder.createOrUpdateValue("$", "max_age", source.getValue("$.corsPolicy.maxAge", String.class));
        }
        if (source.contain("$.corsPolicy.allowCredentials")) {
            builder.createOrUpdateValue("$", "allow_credentials", source.getValue("$.corsPolicy.allowCredentials", Boolean.class));
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
