package com.netease.cloud.nsf.core.plugin;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.IstioHttpClient;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;


/**
 * 路由插件的转换processor
 * <p>
 * <p>
 * todo: 路由path 结合插件path
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/7
 **/
@Component
public class RouteProcessor extends AbstractYxSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Autowired
    private IstioHttpClient istioHttpClient;

    @Override
    public String getName() {
        return "RouteProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        MultiValueMap<String, String> pluginMap = new LinkedMultiValueMap<>();

        ResourceGenerator total = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        // 将路由plugin细分，例如rewrite部分,redirect部分
        List<Object> plugins = total.getValue("$.rule");
        plugins.forEach(innerPlugin -> {
            ResourceGenerator rg = ResourceGenerator.newInstance(innerPlugin, ResourceType.OBJECT, editorContext);
            String innerType = rg.getValue("$.name");
            switch (innerType) {
                case "rewrite": {
                    pluginMap.add(innerType, createRewrite(rg, serviceInfo));
                    break;
                }
                case "redirect": {
                    pluginMap.add(innerType, createRedirect(rg, serviceInfo));
                    break;
                }
                case "return": {
                    pluginMap.add(innerType, createReturn(rg, serviceInfo));
                    break;
                }
                case "pass_proxy": {
                    pluginMap.add(innerType, createPassProxy(rg, serviceInfo));
                    break;
                }
                default:
                    throw new ApiPlaneException("Unsupported inner routing plugin types.");
            }
        });

        // pass_proxy兜底
        if (pluginMap.containsKey("pass_proxy")) {
            pluginMap.put("pass_proxy", pluginMap.remove("pass_proxy"));
        }

        ResourceGenerator result = ResourceGenerator.newInstance("[]", ResourceType.JSON, editorContext);
        pluginMap.values().forEach(item -> item.forEach(o -> result.addJsonElement("$", o)));

        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withContent(result.yamlString())
                .withResourceType(K8sResourceEnum.VirtualService)
                .withFragmentType(FragmentTypeEnum.NEW_MATCH)
                .build();
        holder.setVirtualServiceFragment(wrapper);
        return holder;
    }

    private String createPassProxy(ResourceGenerator rg, ServiceInfo info) {
        List<Endpoint> endpoints = istioHttpClient.getEndpointList();

        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "route", "[]");
        ret.createOrUpdateJson("$", "name", getApiName(info));

        int length = rg.getValue("$.action.pass_proxy_target.length()");
        for (int i = 0; i < length; i++) {
            String targetHost = rg.getValue(String.format("$.action.pass_proxy_target[%d].url", i));
            Integer weight = rg.getValue(String.format("$.action.pass_proxy_target[%d].weight", i));
            Integer port = getPort(endpoints, targetHost);
            // 根据host查找host的port
            ret.addJsonElement("$.route",
                    String.format("{\"destination\":{\"host\":\"%s\",\"port\":{\"number\":%d},\"subset\":\"%s\"},\"weight\":%d}",
                            targetHost, port, info.getSubset(), weight));
        }
        return ret.jsonString();
    }

    private String createReturn(ResourceGenerator rg, ServiceInfo info) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info));
        ret.createOrUpdateJson("$", "return",
                String.format("{\"body\":{\"inlineString\":\"%s\"},\"code\":%s}", rg.getValue("$.action.body"), rg.getValue("$.action.code")));
        ret.createOrUpdateJson("$", "name", getApiName(info));
        return ret.jsonString();
    }

    private String createRedirect(ResourceGenerator rg, ServiceInfo info) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info));
        //todo: authority
        ret.createOrUpdateJson("$", "redirect", String.format("{\"uri\":\"%s\"}", rg.getValue("$.action.target", String.class)));
        ret.createOrUpdateJson("$", "name", getApiName(info));
        return ret.jsonString();
    }

    private String createRewrite(ResourceGenerator rg, ServiceInfo info) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info));
        ret.createOrUpdateJson("$", "requestTransform", String.format("{\"new\":{\"path\":\"%s\"},\"original\":{\"path\":\"%s\"}}"
                , rg.getValue("$.action.target", String.class), rg.getValue("$.action.rewrite_regex")));
        ret.createOrUpdateJson("$", "name", getApiName(info));
        return ret.jsonString();
    }
}
