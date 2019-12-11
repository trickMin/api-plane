package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        ResourceGenerator builder = ResourceGenerator.newInstance(String.format("{\"transformation\":{\"requestTransformations\":[{\"transformationTemplate\":{\"extractors\":{\"call-back\":{\"queryParam\":\"%s\",\"setToContext\":true}}}}],\"responseTransformations\":[{\"transformationTemplate\":{\"extractors\":{\"all-body\":{\"header\":\":body\",\"regex\":\"([\\\\s\\\\S]*)\",\"subgroup\":1}},\"body\":{\"text\":\"{{context(\\\"call-back\\\")}}({{all-body}})\"},\"parseBodyBehavior\":1},\"conditions\":[{\"context\":{\"requestMethod\":\"GET\",\"contextConditions\":[{\"key\":\"call-back\",\"op\":\"IsNotNull\"}]},\"headers\":{\"Content-Type\":{\"regex\":\".*application/json.*\"},\"content-type\":{\"regex\":\".*application/json.*\"}}}]}]}}", callback));
        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(builder.yamlString())
                .withXUserId(getXUserId(source))
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }
}
