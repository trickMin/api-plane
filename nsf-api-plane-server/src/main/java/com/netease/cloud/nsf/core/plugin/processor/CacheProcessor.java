package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CacheProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "Cache";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"low_level_fill\":\"false\", \"key_maker\":{\"exclude_host\":\"false\", \"ignore_case\":\"true\"}, \"cache_ttls\":{\"RedisHttpCache\":{\"default\":\"0\"}, \"LocalHttpCache\":{\"default\":\"0\"}}}");
        // condition request
        if (source.contain("$.condition.request")) {
            builder.createOrUpdateJson("$", "enable_rqx", "{\"headers\":[]}");
        }
        if (source.contain("$.condition.request.headers")) {
            List<Map<String, String>> headers = source.getValue("$.condition.request.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.enable_rqx.headers", String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", headerKey, headerValue));
                } else if ("present_match".equals(matchType)) {
                    builder.addJsonElement("$.enable_rqx.headers", String.format("{\"name\":\"%s\",\"present_match\":true}", headerKey));
                } else if ("present_match_invert".equals(matchType)) {
                    builder.addJsonElement("$.enable_rqx.headers", String.format("{\"name\":\"%s\", \"present_match\":true, \"invert_match\":true}", headerKey));
                } else {
                    builder.addJsonElement("$.enable_rqx.headers", String.format("{\"name\":\"%s\",\"exact_match\":\"%s\"}", headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.condition.request.host")) {
            String matchType = source.getValue("$.condition.request.host.match_type", String.class);
            String host = source.getValue("$.condition.request.host.value", String.class);
            if ("safe_regex_match".equals(matchType)) {
                builder.addJsonElement("$.enable_rqx.headers", String.format("{\"name\":\":authority\",\"regex_match\":\"%s\"}", host));
            } else {
                builder.addJsonElement("$.enable_rqx.headers", String.format("{\"name\":\":authority\",\"exact_match\":\"%s\"}", host));
            }
        }
        if (source.contain("$.condition.request.method")) {
            List<String> method = source.getValue("$.condition.request.method", List.class);
            if (method.size() == 1) {
                builder.addJsonElement("$.enable_rqx.headers", String.format("{\"name\":\":method\",\"exact_match\":\"%s\"}", method.get(0)));
            } else if (method.size() > 1) {
                builder.addJsonElement("$.enable_rqx.headers", String.format("{\"name\":\":method\",\"regex_match\":\"%s\"}", String.join("|", method)));
            }
        }
        if (source.contain("$.condition.request.path")) {
            String matchType = source.getValue("$.condition.request.path.match_type", String.class);
            String path = source.getValue("$.condition.request.path.value", String.class);
            if ("safe_regex_match".equals(matchType)) {
                builder.addJsonElement("$.enable_rqx.headers", String.format("{\"name\":\":path\",\"regex_match\":\"%s\"}", path));
            } else {
                builder.addJsonElement("$.enable_rqx.headers", String.format("{\"name\":\":path\",\"exact_match\":\"%s\"}", path));
            }
        }
        // condition response
        if (source.contain("$.condition.response")) {
            builder.createOrUpdateJson("$", "enable_rpx", "{\"headers\":[]}");
        }
        if (source.contain("$.condition.response.headers")) {
            List<Map<String, String>> headers = source.getValue("$.condition.response.headers", List.class);
            headers.forEach(item -> {
                String matchType = item.get("match_type");
                String headerKey = item.get("headerKey");
                String headerValue = item.get("value");
                if ("safe_regex_match".equals(matchType)) {
                    builder.addJsonElement("$.enable_rpx.headers", String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", headerKey, headerValue));
                } else if ("present_match".equals(matchType)) {
                    builder.addJsonElement("$.enable_rpx.headers", String.format("{\"name\":\"%s\",\"present_match\":true}", headerKey));
                } else if ("present_match_invert".equals(matchType)) {
                    builder.addJsonElement("$.enable_rpx.headers", String.format("{\"name\":\"%s\", \"present_match\":true, \"invert_match\":true}", headerKey));
                } else {
                    builder.addJsonElement("$.enable_rpx.headers", String.format("{\"name\":\"%s\",\"exact_match\":\"%s\"}", headerKey, headerValue));
                }
            });
        }
        if (source.contain("$.condition.response.code")) {
            String code = source.getValue("$.condition.response.code.value", String.class);
            builder.addJsonElement("$.enable_rpx.headers", String.format("{\"name\":\":status\",\"regex_match\":\"%s|\"}", code));
        }

        // redis http cache ttl
        String redisDefaultTtl = source.getValue("$.ttl.redis.default", String.class);
        builder.createOrUpdateJson("$.cache_ttls.RedisHttpCache", "default", redisDefaultTtl);
        if (source.contain("$.ttl.redis.custom")) {
            builder.createOrUpdateJson("$.cache_ttls.RedisHttpCache", "customs", "{}");
             List<Map<String, String>> customTtl = source.getValue("$.ttl.redis.custom", List.class);
             customTtl.forEach(item -> {
                 String code = item.get("code");
                 String value = item.get("value");
                 builder.createOrUpdateJson("$.cache_ttls.RedisHttpCache.customs", code, value);
             });
        }
        // local http cache ttl
        String localDefaultTtl = source.getValue("$.ttl.local.default", String.class);
        builder.createOrUpdateJson("$.cache_ttls.LocalHttpCache", "default", localDefaultTtl);
        if (source.contain("$.ttl.local.custom")) {
            builder.createOrUpdateJson("$.cache_ttls.LocalHttpCache", "customs", "{}");
            List<Map<String, String>> customTtl = source.getValue("$.ttl.local.custom", List.class);
            customTtl.forEach(item -> {
                String code = item.get("code");
                String value = item.get("value");
                builder.createOrUpdateJson("$.cache_ttls.LocalHttpCache.customs", code, value);
            });
        }

        // key maker
        Boolean excludeHost = source.getValue("$.keyMaker.excludeHost", Boolean.class);
        builder.updateValue("$.key_maker.exclude_host", excludeHost);
        Boolean ignoreCase = source.getValue("$.keyMaker.ignoreCase", Boolean.class);
        builder.updateValue("$.key_maker.ignore_case", ignoreCase);
        if (source.contain("$.keyMaker.headers")) {
            builder.createOrUpdateJson("$.key_maker", "headers_keys", "[]");
            List<String> headers = source.getValue("$.keyMaker.headers", List.class);
            headers.forEach(item -> builder.addElement("$.key_maker.headers_keys", item));
        }
        if (source.contain("$.keyMaker.queryString")) {
            builder.createOrUpdateJson("$.key_maker", "query_params", "[]" );
            List<String> queryStrings = source.getValue("$.keyMaker.queryString", List.class);
            queryStrings.forEach(item -> builder.addElement("$.key_maker.query_params", item));
        }

        // low_level_fill
        Boolean lowLevelFill = source.getValue("$.lowLevelFill", Boolean.class);
        builder.updateValue("$.low_level_fill", lowLevelFill);

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
