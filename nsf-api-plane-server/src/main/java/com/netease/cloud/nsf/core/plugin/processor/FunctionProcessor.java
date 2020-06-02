package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.core.plugin.PluginGenerator;
import com.netease.cloud.nsf.meta.ServiceInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jayway.jsonpath.Configuration;

import org.springframework.stereotype.Component;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/9/26
 **/
@Component
public class FunctionProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    private static final String FUNCTION_TEMPLATE =
            "function envoy_on_request(request_handle)\n" +
                    "  %s\n" +
                    "end\n" +
                    "function envoy_on_response(response_handle)\n" +
                    "  %s\n" +
                    "end\n";

    private static EditorContext editorContext = new EditorContext(new ObjectMapper(),
            new YAMLMapper().configure(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE, true),
            Configuration.defaultConfiguration());

    @Override
    public String getName() {
        return "FunctionProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        PluginGenerator rg = PluginGenerator.newInstance(plugin);
        String request = ((String) rg.getValue("$.envoy_on_request")).replace("\n", "\n  ");
        String response = ((String) rg.getValue("$.envoy_on_response")).replace("\n", "\n  ");
        PluginGenerator ret = PluginGenerator.newInstance("{\"code\":{\"inline_string\": \"\"}}", ResourceType.JSON,
                editorContext);
        ret.createOrUpdateValue("$.code", "inline_string", String.format(FUNCTION_TEMPLATE, request, response));

        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withXUserId(getAndDeleteXUserId(rg))
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(ret.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }
}
