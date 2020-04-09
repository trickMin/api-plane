package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.meta.Plugin;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/8
 **/
@Component
public class CircuitBreakerProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "CircuitBreakerProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        PluginGenerator builder = PluginGenerator.newInstance("{}");
        buildResponse(source, builder);
        buildConfig(source, builder);
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

    private void buildResponse(PluginGenerator source, PluginGenerator builder) {
        if (!source.contain("$.response")) return;
        builder.createOrUpdateJson("$", "response", "{}");
        if (source.contain("$.response.code")) {
            String code = source.getValue("$.response.code");
            builder.createOrUpdateValue("$.response", "http_status", Integer.parseInt(code));
        }
        if (source.contain("$.response.body")) {
            String body = source.getValue("$.response.body");
            builder.createOrUpdateJson("$.response", "body", String.format("{\"inline_string\":\"%s\"}", StringEscapeUtils.escapeJava(body)));
        }
        if (source.contain("$.response.headers")) {
            builder.createOrUpdateJson("$.response", "headers", "[]");
            int length = source.getValue("$.response.headers.length()");
            for (int i = 0; i < length; i++) {
                String key = source.getValue(String.format("$.response.headers[%s].key", i));
                String value = source.getValue(String.format("$.response.headers[%s].value", i));
                builder.addJsonElement("$.response.headers", String.format("{\"key\":\"%s\",\"value\":\"%s\"}", key, value));
            }
        }
    }

    private void buildConfig(PluginGenerator source, PluginGenerator builder) {
        if (!source.contain("$.config")) return;
        if (source.contain("$.config.consecutive_slow_requests")) {
            Integer consecutive_slow_requests = source.getValue("$.config.consecutive_slow_requests");
            builder.createOrUpdateValue("$", "consecutive_slow_requests", consecutive_slow_requests);
        }
        if (source.contain("$.config.average_response_time")) {
            String average_response_time = source.getValue("$.config.average_response_time");
            builder.createOrUpdateValue("$", "average_response_time", average_response_time);
        }
        if (source.contain("$.config.min_request_amount")) {
            Integer min_request_amount = source.getValue("$.config.min_request_amount");
            builder.createOrUpdateValue("$", "min_request_amount", min_request_amount);
        }
        if (source.contain("$.config.error_percent_threshold")) {
            Integer error_percent_threshold = source.getValue("$.config.error_percent_threshold");
            builder.createOrUpdateJson("$", "error_percent_threshold", String.format("{\"value\":%s}", error_percent_threshold));
        }
        if (source.contain("$.config.break_duration")) {
            String break_duration = source.getValue("$.config.break_duration");
            builder.createOrUpdateValue("$", "break_duration", break_duration);
        }
        if (source.contain("$.config.lookback_duration")) {
            String lookback_duration = source.getValue("$.config.lookback_duration");
            builder.createOrUpdateValue("$", "lookback_duration", lookback_duration);
        }
    }

}