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
        Matcher matcher = Pattern.compile("\\$\\d").matcher(source.getValue("$.action.target"));
        int regexCount = 0;
        while (matcher.find()) {
            regexCount++;
        }
        String original = source.getValue("$.action.rewrite_regex");
        String target = source.getValue("$.action.target", String.class).replaceAll("(\\$\\d)", "{{$1}}");
        builder.createOrUpdateJson("$", "request_transformations",
                String.format("[{\"conditions\":[{\"headers\":[{\"name\":\":path\",\"regex_match\":\"%s\"}]}],\"transformation_template\":{\"parse_body_behavior\":\"DontParse\",\"extractors\":{},\"headers\":{}}}]", original));
        // $.action.target : 转换结果，格式如/$2/$1
        for (int i = 1; i <= regexCount; i++) {
            String key = "$" + i;
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
}
