package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/11/19
 **/
@Component
public class RewriteProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "RewriteProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator builder = PluginGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        PluginGenerator source = PluginGenerator.newInstance(plugin);
        Matcher matcher = Pattern.compile("\\$(\\d)").matcher(source.getValue("$.action.target"));
        int regexCount = 0;
        while (matcher.find()) {
            int group = Integer.parseInt(matcher.group(1));
            if(group > regexCount){
                regexCount = group;
            }
        }

        String original = source.getValue("$.action.rewrite_regex");
        String target = source.getValue("$.action.target", String.class).replaceAll("(\\$\\d)", "{{_1}}");
        builder.createOrUpdateJson("$", "request_transformations",
                String.format("[{\"conditions\":[{\"headers\":[{\"name\":\":path\",\"regex_match\":\"%s\"}],\"query_parameters\":[]}],\"transformation_template\":{\"passthrough\":{},\"parse_body_behavior\":\"DontParse\",\"extractors\":{},\"headers\":{}}}]", original));
        buildConditions(source, builder);

        // $.action.target : 转换结果，格式如/$2/$1
        for (int i = 1; i <= regexCount; i++) {
            String key = "_" + i;
            String value = String.format("{\"header\":\":path\",\"regex\":\"%s\",\"subgroup\":%s}", original, i);
            builder.createOrUpdateJson("$.request_transformations[0].transformation_template.extractors", key, value);
        }
        builder.createOrUpdateJson("$.request_transformations[0].transformation_template.headers", ":path", String.format("{\"text\":\"%s\"}", target));

        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(source))
                .withContent(builder.yamlString())
                .withResourceType(K8sResourceEnum.VirtualService)
                .withFragmentType(FragmentTypeEnum.VS_API)
                .build();
        holder.setVirtualServiceFragment(wrapper);
        return holder;
    }

    private void buildConditions(PluginGenerator source, PluginGenerator builder) {
        if (!source.contain("$.conditions")) return;
        if (source.contain("$.conditions.headers")) {
            int itemCount = source.getValue("$.conditions.headers.length()", Integer.class);
            for (int i = 0; i < itemCount; i++) {
                String op = source.getValue(String.format("$.conditions.headers[%s].op", i));
                String key = source.getValue(String.format("$.conditions.headers[%s].key", i));
                String text = source.getValue(String.format("$.conditions.headers[%s].text", i));
                builder.addJsonElement("$.request_transformations[0].conditions[0].headers", String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", key, getRegexByOp(op, text)));
            }
        }
        if (source.contain("$.conditions.querystrings")) {
            int itemCount = source.getValue("$.conditions.querystrings.length()", Integer.class);
            for (int i = 0; i < itemCount; i++) {
                String op = source.getValue(String.format("$.conditions.querystrings[%s].op", i));
                String key = source.getValue(String.format("$.conditions.querystrings[%s].key", i));
                String text = source.getValue(String.format("$.conditions.querystrings[%s].text", i));
                builder.addJsonElement("$.request_transformations[0].conditions[0].query_parameters", String.format("{\"name\":\"%s\",\"value\":\"%s\",\"regex\":true}", key, getRegexByOp(op, text)));
            }
        }
        if (source.contain("$.conditions.url")) {
            int itemCount = source.getValue("$.conditions.url.length()", Integer.class);
            for (int i = 0; i < itemCount; i++) {
                String op = source.getValue(String.format("$.conditions.url[%s].op", i));
                String key = ":path";
                String text = source.getValue(String.format("$.conditions.url[%s].text", i));
                builder.addJsonElement("$.request_transformations[0].conditions[0].headers", String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", key, getRegexByOp(op, text)));
            }
        }
        if (source.contain("$.conditions.host")) {
            int itemCount = source.getValue("$.conditions.host.length()", Integer.class);
            for (int i = 0; i < itemCount; i++) {
                String op = source.getValue(String.format("$.conditions.host[%s].op", i));
                String key = ":authority";
                String text = source.getValue(String.format("$.conditions.host[%s].text", i));
                builder.addJsonElement("$.request_transformations[0].conditions[0].headers", String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", key, getRegexByOp(op, text)));
            }
        }
        if (source.contain("$.conditions.method")) {
            int itemCount = source.getValue("$.conditions.method.length()", Integer.class);
            for (int i = 0; i < itemCount; i++) {
                String op = source.getValue(String.format("$.conditions.method[%s].op", i));
                String key = ":method";
                String text = source.getValue(String.format("$.conditions.method[%s].text", i));
                builder.addJsonElement("$.request_transformations[0].conditions[0].headers", String.format("{\"name\":\"%s\",\"regex_match\":\"%s\"}", key, getRegexByOp(op, text)));
            }
        }
    }
}
