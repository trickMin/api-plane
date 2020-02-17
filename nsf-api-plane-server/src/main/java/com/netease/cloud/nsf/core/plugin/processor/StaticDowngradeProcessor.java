package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.util.List;
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
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"downgrade_rpx\":{\"headers\":[]},\"static_response\":{\"http_status\":0}}");
        if (source.contain("$.condition.request")) {
            builder.createOrUpdateJson("$", "downgrade_rqx", "{\"headers\":[]}");
        }
        if (source.contain("$.condition.request.headers")) {
            List<Map<String, String>> headers = source.getValue("$.condition.request.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("headerValue");
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\"%s\",\"safe_regex_match\":{\"regex\":\"%s\",\"google_re2\":{}}}", headerKey, headerValue));
                    System.out.println("yes");
                } else {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\"%s\",\"exact_match\":\"%s\"}", headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.condition.request.host")) {
            String matchType = source.getValue("$.condition.request.host.match_type", String.class);
            String host = source.getValue("$.condition.request.host.value", String.class);
            if ("safe_regex_match".equals(matchType)) {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":authority\",\"safe_regex_match\":{\"regex\":\"%s\",\"google_re2\":{}}}", host));
            } else {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":authority\",\"exact_match\":\"%s\"}", host));
            }
        }
        if (source.contain("$.condition.request.method")) {
            List<String> method = source.getValue("$.condition.request.method", List.class);
            if (method.size() == 1) {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":method\",\"exact_match\":\"%s\"}", method));
            } else if (method.size() > 1) {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":method\",\"safe_regex_match\":{\"regex\":\"%s\",\"google_re2\":{}}}", String.join("|", method)));
            }
        }
        if (source.contain("$.condition.request.path")) {
            String matchType = source.getValue("$.condition.request.path.match_type", String.class);
            String path = source.getValue("$.condition.request.path.value", String.class);
            if ("safe_regex_match".equals(matchType)) {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":path\",\"safe_regex_match\":{\"regex\":\"%s\",\"google_re2\":{}}}", path));
            } else {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":path\",\"exact_match\":\"%s\"}", path));
            }
        }
        if (source.contain("$.condition.response.headers")) {
            List<Map<String, String>> headers = source.getValue("$.condition.response.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("headerValue");
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.downgrade_rpx.headers", String.format("{\"name\":\"%s\",\"safe_regex_match\":{\"regex\":\"%s\",\"google_re2\":{}}}", headerKey, headerValue));
                    System.out.println("yes");
                } else {
                    builder.addJsonElement("$.downgrade_rpx.headers", String.format("{\"name\":\"%s\",\"exact_match\":\"%s\"}", headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.condition.response.code")) {
            String matchType = source.getValue("$.condition.response.code.match_type", String.class);
            String code = source.getValue("$.condition.response.code.value", String.class);
            if ("safe_regex_match".equals(matchType)) {
                builder.addJsonElement("$.downgrade_rpx.headers", String.format("{\"name\":\":status\",\"safe_regex_match\":{\"regex\":\"%s\",\"google_re2\":{}}}", code));
            } else {
                builder.addJsonElement("$.downgrade_rpx.headers", String.format("{\"name\":\":status\",\"exact_match\":\"(%s)\"}", code));
            }
        }
        builder.updateValue("$.static_response.http_status", source.getValue("$.response.code", Integer.class));
        if (source.contain("$.response.headers")) {
            builder.createOrUpdateJson("$.static_response", "headers", "[]");
            Map<String, String> headers = source.getValue("$.response.headers", Map.class);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addJsonElement("$.static_response.headers", String.format("{\"key\":\"%s\",\"value\":\"%s\"}", entry.getKey(), entry.getValue()));
            }
        }
        if (source.contain("$.response.body")) {
            String body = StringEscapeUtils.escapeJava(source.getValue("$.response.body", String.class));
            builder.createOrUpdateJson("$.static_response", "body", String.format("{\"inline_string\": \"%s\"}", body));
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
