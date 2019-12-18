package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/11/11
 **/
@Component
public class JsonpProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "JsonpProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        String callback = source.getValue("callback");
        ResourceGenerator builder = ResourceGenerator.newInstance(String.format("{\"transformation\":{\"request_transformations\":[{\"transformation_template\":{\"extractors\":{\"call-back\":{\"query_param\":\"%s\",\"set_to_context\":true}},\"parse_body_behavior\":\"DontParse\"}}],\"response_transformations\":[{\"transformation_template\":{\"extractors\":{\"all-body\":{\"header\":\":body\",\"regex\":\"([\\\\s\\\\S]*)\",\"subgroup\":1}},\"body\":{\"text\":\"{{call-back}}({{all-body}})\"},\"parse_body_behavior\":\"DontParse\"},\"conditions\":[{\"context\":{\"request_method\":\"GET\",\"context_conditions\":[{\"key\":\"call-back\",\"op\":\"IsNotNull\"}]},\"headers\":{\"Content-Type\":{\"regex\":\".*application/json.*\"},\"content-type\":{\"regex\":\".*application/json.*\"}}}]}]}}", callback));
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
