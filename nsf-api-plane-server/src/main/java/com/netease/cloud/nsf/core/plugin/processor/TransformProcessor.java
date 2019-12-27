package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/12/17
 **/
@Component
public class TransformProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "TransformProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"request_transformations\":[{\"transformation_template\":{\"extractors\":{},\"headers\":{},\"query_param_operators\":{},\"parse_body_behavior\":\"DontParse\"}}]}");
        List<String> texts = new ArrayList<>();
        if (source.contain("$.headers")) {
            texts.addAll(source.getValue("$.headers[*].text"));
        }
        if (source.contain("$.querystrings")) {
            texts.addAll(source.getValue("$.querystrings[*].text"));
        }
        List<String> placeholders = new ArrayList<>();
        texts.stream().filter(Objects::nonNull).forEach(text -> {
            Matcher matcher = Pattern.compile("\\{\\{(.*)\\}\\}").matcher(text);
            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }
        });
        buildExtractors(placeholders, builder, serviceInfo);
        buildHeaders(source, builder);
        buildQueryParam(source, builder);
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

    private void buildExtractors(List<String> placeholders, ResourceGenerator builder, ServiceInfo serviceInfo) {
        for (String placeholder : placeholders) {
            if (placeholder.startsWith("url")) {
                Matcher matcher = Pattern.compile("url\\[(.*)\\]").matcher(placeholder);
                if (matcher.find()) {
                    Integer index = Integer.parseInt(matcher.group(1)) + 1;
                    String value = String.format("{\"header\":\":path\",\"regex\":\"%s\",\"subgroup\":%s}", serviceInfo.getUri(), index);
                    builder.createOrUpdateJson("$.request_transformations[0].transformation_template.extractors", placeholder, value);
                }
            } else if (placeholder.startsWith("querystrings")) {
                Matcher matcher = Pattern.compile("querystrings\\[(.*)\\]").matcher(placeholder);
                if (matcher.find()) {
                    String queryParam = matcher.group(1);
                    String value = String.format("{\"queryParam\":\"%s\",\"regex\":\"(.*)\",\"subgroup\":1}", queryParam);
                    builder.createOrUpdateJson("$.request_transformations[0].transformation_template.extractors", placeholder, value);
                }
            } else if (placeholder.startsWith("headers")) {
                Matcher matcher = Pattern.compile("headers\\[(.*)\\]").matcher(placeholder);
                if (matcher.find()) {
                    String header = matcher.group(1);
                    String value = String.format("{\"header\":\"%s\",\"regex\":\"(.*)\",\"subgroup\":1}", header);
                    builder.createOrUpdateJson("$.request_transformations[0].transformation_template.extractors", placeholder, value);
                }
            }
        }
    }

    private void buildHeaders(ResourceGenerator source, ResourceGenerator builder) {
        if (source.contain("headers")) {
            Integer size = source.getValue("$.headers.size()");
            for (Integer i = 0; i < size; i++) {
                String key = source.getValue(String.format("$.headers[%s].key", i));
                String text = source.getValue(String.format("$.headers[%s].text", i));
                String action = source.getValue(String.format("$.headers[%s].action", i));

                String value;
                if (StringUtils.isEmpty(text)) {
                    value = String.format("{\"action\":\"%s\"}", action);
                } else {
                    value = String.format("{\"text\":\"%s\",\"action\":\"%s\"}", text, action);
                }
                builder.createOrUpdateJson("$.request_transformations[0].transformation_template.headers", key, value);
            }
        }
    }

    private void buildQueryParam(ResourceGenerator source, ResourceGenerator builder) {
        if (source.contain("querystrings")) {
            Integer size = source.getValue("$.querystrings.size()");
            for (Integer i = 0; i < size; i++) {
                String key = source.getValue(String.format("$.querystrings[%s].key", i));
                String text = source.getValue(String.format("$.querystrings[%s].text", i));
                String action = source.getValue(String.format("$.querystrings[%s].action", i));

                String value;
                if (StringUtils.isEmpty(text)) {
                    value = String.format("{\"action\":\"%s\"}", action);
                } else {
                    value = String.format("{\"text\":\"%s\",\"action\":\"%s\"}", text, action);
                }
                builder.createOrUpdateJson("$.request_transformations[0].transformation_template.query_param_operators", key, value);
            }
        }
    }
}
