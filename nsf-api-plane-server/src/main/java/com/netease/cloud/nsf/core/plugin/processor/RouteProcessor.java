package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
public class RouteProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Autowired
    private ResourceManager resourceManager;

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

        ResourceGenerator result = ResourceGenerator.newInstance("[]", ResourceType.JSON, editorContext);
        pluginMap.values().forEach(item -> item.forEach(o -> result.addJsonElement("$", o)));

        FragmentHolder holder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withContent(result.yamlString())
                .withResourceType(K8sResourceEnum.VirtualService)
                .withFragmentType(FragmentTypeEnum.VS_MATCH)
                .build();
        holder.setVirtualServiceFragment(wrapper);
        return holder;
    }

    private String createPassProxy(ResourceGenerator rg, ServiceInfo info) {
        List<Endpoint> endpoints = resourceManager.getEndpointList();

        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info));
        ret.createOrUpdateJson("$", "route", "[]");

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
                String.format("{\"body\":{\"inlineString\":\"%s\"},\"code\":%s}", rg.getValue("$.action.return_target.body"), rg.getValue("$.action.return_target.code")));
        if (rg.contain("$.action.header")) {
            ret.createOrUpdateJson("$", "appendHeaders", "{}");
            List<Object> headers = rg.getValue("$.action.header[*]");
            for (Object header : headers) {
                ResourceGenerator h = ResourceGenerator.newInstance(header, ResourceType.OBJECT);
                ret.createOrUpdateValue("$.appendHeaders", h.getValue("$.name"), h.getValue("$.value"));
            }
        }
        return ret.jsonString();
    }

    private String createRedirect(ResourceGenerator rg, ServiceInfo info) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info));
        String target = rg.getValue("$.action.target", String.class);
        try {
            URI uri = new URI(target);
            String authority = uri.getAuthority();
            String path = uri.getPath();
            String query = uri.getQuery();
            String pathAndQuery = path;
            if (!StringUtils.isEmpty(query)) {
                pathAndQuery = String.format("%s?%s", path, query);
            }
            if (!StringUtils.isEmpty(authority)) {
                ret.createOrUpdateJson("$", "redirect", String.format("{\"uri\":\"%s\",\"authority\":\"%s\"}", pathAndQuery, authority));
            } else {
                ret.createOrUpdateJson("$", "redirect", String.format("{\"uri\":\"%s\"}", pathAndQuery));
            }
        } catch (URISyntaxException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
        return ret.jsonString();
    }

    private String createRewrite(ResourceGenerator rg, ServiceInfo info) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info));
        ret.createOrUpdateJson("$", "transformation", "{\"requestTransformations\":[{\"transformationTemplate\":{\"extractors\":{},\"headers\":{}}}]}");
        // $.action.target : 转换结果，格式如/$2/$1
        Matcher matcher = Pattern.compile("\\$\\d").matcher(rg.getValue("$.action.target"));
        int regexCount = 0;
        while (matcher.find()) {
            regexCount++;
        }
        String original = rg.getValue("$.action.rewrite_regex");
        String target = rg.getValue("$.action.target", String.class).replaceAll("(\\$\\d)", "{{$1}}");
        for (int i = 1; i <= regexCount; i++) {
            String key = "$" + i;
            String value = String.format("{\"header\":\":path\",\"regex\":\"%s\",\"subgroup\":%s}", original, i);
            ret.createOrUpdateJson("$.transformation.requestTransformations[0].transformationTemplate.extractors", key, value);
        }
        ret.createOrUpdateJson("$.transformation.requestTransformations[0].transformationTemplate.headers", ":path", String.format("{\"text\":\"%s\"}", target));
        return ret.jsonString();
    }
}
