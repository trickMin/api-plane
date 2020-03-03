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
import java.util.Map;
import java.util.Optional;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/2/25
 **/
@Component
public class DynamicDowngradeProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "DynamicDowngradeProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"downgrade_rpx\":{\"headers\":[]},\"cache_rpx_rpx\":{\"headers\":[]},\"cache_ttls\":{\"RedisHttpCache\":{}},\"key_maker\":{\"query_params\":[],\"headers_keys\":[]}}");
        createCondition(source, builder);
        createCacheRpx(source, builder);
        createCacheTtls(source, builder);
        createKeyMaker(source, builder);


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

    private void createCondition(ResourceGenerator source, ResourceGenerator builder) {
        if (source.contain("$.condition.request")) {
            builder.createOrUpdateJson("$", "downgrade_rqx", "{\"headers\":[]}");
        }
        if (source.contain("$.condition.request.headers")) {
            List<Map<String, String>> headers = source.getValue("$.condition.request.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", headerKey, headerValue));
                } else {
                    builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\"%s\",\"exact_match\":\"%s\"}", headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.condition.request.host")) {
            String matchType = source.getValue("$.condition.request.host.match_type", String.class);
            String host = source.getValue("$.condition.request.host.value", String.class);
            if ("safe_regex_match".equals(matchType)) {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":authority\",\"regex_match\":\"%s\"}", host));
            } else {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":authority\",\"exact_match\":\"%s\"}", host));
            }
        }
        if (source.contain("$.condition.request.method")) {
            List<String> method = source.getValue("$.condition.request.method", List.class);
            if (method.size() == 1) {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":method\",\"exact_match\":\"%s\"}", method.get(0)));
            } else if (method.size() > 1) {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":method\",\"regex_match\":\"%s\"}", String.join("|", method)));
            }
        }
        if (source.contain("$.condition.request.path")) {
            String matchType = source.getValue("$.condition.request.path.match_type", String.class);
            String path = source.getValue("$.condition.request.path.value", String.class);
            if ("safe_regex_match".equals(matchType)) {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":path\",\"regex_match\":\"%s\"}", path));
            } else {
                builder.addJsonElement("$.downgrade_rqx.headers", String.format("{\"name\":\":path\",\"exact_match\":\"%s\"}", path));
            }
        }
        if (source.contain("$.condition.response.headers")) {
            List<Map<String, String>> headers = source.getValue("$.condition.response.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.downgrade_rpx.headers", String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", headerKey, headerValue));
                } else {
                    builder.addJsonElement("$.downgrade_rpx.headers", String.format("{\"name\":\"%s\",\"exact_match\":\"%s\"}", headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.condition.response.code")) {
            String matchType = source.getValue("$.condition.response.code.match_type", String.class);
            String code = source.getValue("$.condition.response.code.value", String.class);
            builder.addJsonElement("$.downgrade_rpx.headers", String.format("{\"name\":\":status\",\"regex_match\":\"%s|\"}", code));
        }
    }

    private void createCacheRpx(ResourceGenerator source, ResourceGenerator builder) {
        if (source.contain("$.cache.condition.response.code")) {
            String mathchType = source.getValue("$.cache.condition.response.code.match_type");
            String code = source.getValue("$.cache.condition.response.code.value");
            builder.addJsonElement("$.cache_rpx_rpx.headers", String.format("{\"name\":\":status\",\"regex_match\":\"%s|\"}", code));
        }
        if (source.contain("$.cache.condition.response.headers")) {
            List<Map<String, String>> headers = source.getValue("$.cache.condition.response.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.cache_rpx_rpx.headers", String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", headerKey, headerValue));
                } else {
                    builder.addJsonElement("$.cache_rpx_rpx.headers", String.format("{\"name\":\"%s\",\"exact_match\":\"%s\"}", headerKey, headerValue));
                }
            });
        }
    }

    private void createCacheTtls(ResourceGenerator source, ResourceGenerator builder) {
        if (source.contain("$.cache.ttls.default")) {
            builder.createOrUpdateValue("$.cache_ttls.RedisHttpCache", "default", 30000);
        }
        if (source.contain("$.cache.ttls.custom")) {
            builder.createOrUpdateJson("$.cache_ttls.RedisHttpCache", "customs", "{}");
            List<Map<String, Object>> customs = source.getValue("$.cache.ttls.custom", List.class);
            customs.forEach(item -> {
                String code = (String) Optional.ofNullable(item.get("code")).orElse("200");
                Object ttl = item.get("ttl");
                builder.createOrUpdateValue("$.cache_ttls.RedisHttpCache.customs", code, ttl);
            });
        }
    }

    private void createKeyMaker(ResourceGenerator source, ResourceGenerator builder) {
        if (source.contain("$.cache.cache_key.query_params")) {
            List<String> queryParams = source.getValue("$.cache.cache_key.query_params", List.class);
            queryParams.forEach(item -> {
                builder.addElement("$.key_maker.query_params", item);
            });
        }
        if (source.contain("$.cache.cache_key.headers")) {
            List<String> headers = source.getValue("$.cache.cache_key.headers", List.class);
            headers.forEach(item -> {
                builder.addJsonElement("$.key_maker.headers_keys", item);
            });
        }
    }
}