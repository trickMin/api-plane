package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

@Component
public class AuthProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "SuperAuth";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"need_authorization\":\"false\", \"failure_auth_allow\":\"false\"}");
         String authType = source.getValue("$.authnType", String.class);
        if ("aksk_authn_type".equals(authType)) {
            builder.createOrUpdateJson("$", "aksk_authn_type", "{}");
        } else if ("jwt_authn_type".equals(authType)){
            builder.createOrUpdateJson("$", "jwt_authn_type", "{}");
        }else {
            builder.createOrUpdateJson("$", "oauth2_authn_type", "{}");
        }

        builder.updateValue("$.need_authorization", source.getValue("$.useAuthz", Boolean.class));
        Boolean failureAuthAllow = source.getValue("$.failureAuthAllow", Boolean.class);
        failureAuthAllow = null == failureAuthAllow ? false : failureAuthAllow;
        builder.updateValue("$.failure_auth_allow", failureAuthAllow);

        if (source.contain("$.bufferSetting.maxRequestBytes")) {
            String maxRequestBody = source.getValue("$.bufferSetting.maxRequestBytes", String.class);
            builder.createOrUpdateJson("$", "with_request_body", String.format("{\"max_request_bytes\":\"%s\", \"allow_partial_message\":\"false\"}", maxRequestBody));
        }
        Boolean allowPartialMessage = source.getValue("$.bufferSetting.allowPartialMessage", Boolean.class);
        builder.updateValue("$.with_request_body.allow_partial_message", allowPartialMessage);
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
