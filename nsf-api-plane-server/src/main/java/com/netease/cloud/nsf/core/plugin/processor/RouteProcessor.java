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
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 路由插件的转换processor
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
        //todo: XUser
        MultiValueMap<String, String> pluginMap = new LinkedMultiValueMap<>();

        ResourceGenerator total = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        // 如果路由插件自身配置了priority，则使用配置的priority，如果没有配置，则使用占位符传递的priority
        Integer priority = getPriority(total);
        if (Objects.nonNull(priority)) serviceInfo.setPriority(String.valueOf(priority));

        // 将路由plugin细分，例如rewrite部分,redirect部分
        List<Object> plugins = total.getValue("$.rule");
        plugins.forEach(innerPlugin -> {
            ResourceGenerator rg = ResourceGenerator.newInstance(innerPlugin, ResourceType.OBJECT, editorContext);
            String innerType = rg.getValue("$.name");
            switch (innerType) {
                case "rewrite": {
                    pluginMap.add(innerType, createRewrite(rg, serviceInfo, null));
                    break;
                }
                case "redirect": {
                    pluginMap.add(innerType, createRedirect(rg, serviceInfo, null));
                    break;
                }
                case "return": {
                    pluginMap.add(innerType, createReturn(rg, serviceInfo, null));
                    break;
                }
                case "pass_proxy": {
                    pluginMap.add(innerType, createPassProxy(rg, serviceInfo, null));
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
                .withXUserId(getAndDeleteXUserId(total))
                .build();
        holder.setVirtualServiceFragment(wrapper);
        return holder;
    }

    private String createPassProxy(ResourceGenerator rg, ServiceInfo info, String xUserId) {
        List<Endpoint> endpoints = resourceManager.getEndpointList();

        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info, xUserId));
        ret.createOrUpdateJson("$", "priority", info.getPriority());
        ret.createOrUpdateJson("$", "route", "[]");

        int length = rg.getValue("$.action.pass_proxy_target.length()");
        for (int i = 0; i < length; i++) {
            String targetHost = rg.getValue(String.format("$.action.pass_proxy_target[%d].url", i));
            Integer weight = rg.getValue(String.format("$.action.pass_proxy_target[%d].weight", i));
            Integer port = resourceManager.getServicePort(endpoints, targetHost);
            // 根据host查找host的port
            ret.addJsonElement("$.route",
                    String.format("{\"destination\":{\"host\":\"%s\",\"port\":{\"number\":%d},\"subset\":\"%s\"},\"weight\":%d}",
                            targetHost, port, info.getSubset(), weight));
        }
        return ret.jsonString();
    }

    private String createReturn(ResourceGenerator rg, ServiceInfo info, String xUserId) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info, xUserId));
        ret.createOrUpdateJson("$", "priority", info.getPriority());
        ret.createOrUpdateJson("$", "return",
                String.format("{\"body\":{\"inlineString\":\"%s\"},\"code\":%s}", StringEscapeUtils.escapeJava(rg.getValue("$.action.return_target.body")), rg.getValue("$.action.return_target.code")));
        if (rg.contain("$.action.return_target.header")) {
            ret.createOrUpdateJson("$", "appendRequestHeaders", "{}");
            List<Object> headers = rg.getValue("$.action.return_target.header[*]");
            for (Object header : headers) {
                ResourceGenerator h = ResourceGenerator.newInstance(header, ResourceType.OBJECT);
                ret.createOrUpdateValue("$.appendRequestHeaders", h.getValue("$.name"), h.getValue("$.value"));
            }
        }
        return ret.jsonString();
    }

    private String createRedirect(ResourceGenerator rg, ServiceInfo info, String xUserId) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info, xUserId));
        ret.createOrUpdateJson("$", "priority", info.getPriority());
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

    private String createRewrite(ResourceGenerator rg, ServiceInfo info, String xUserId) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info, xUserId));
        ret.createOrUpdateJson("$", "priority", info.getPriority());
        ret.createOrUpdateJson("$", "transformation", "{\"requestTransformations\":[{\"transformationTemplate\":{\"extractors\":{},\"headers\":{},\"parseBodyBehavior\":1}}]}");
        String extractor = rg.getValue("$.action.rewrite_regex");
        String transformPath = rg.getValue("$.action.target", String.class);
        // 兼容旧的格式，例如rewrite_regex: /anything/{code} action.target:/anything/gg/{{code}}
        if (Pattern.compile("\\{(.*)\\}").matcher(extractor).find() && Pattern.compile("\\{\\{(.*)\\}\\}").matcher(transformPath).find()) {
            // 将/anything/{code}转换为/anything/(.*)
            Matcher extract = Pattern.compile("\\{(.*?)\\}").matcher(extractor);
            String path = extractor.replaceAll("\\{(.*?)\\}", "\\(\\.\\*\\)");
            int regexCount = 1;
            while (extract.find()) {
                String key = extract.group(1);
                String value = String.format("{\"header\":\":path\",\"regex\":\"%s\",\"subgroup\":%s}", path, regexCount++);
                ret.createOrUpdateJson("$.transformation.requestTransformations[0].transformationTemplate.extractors", key, value);
            }
            ret.createOrUpdateJson("$.transformation.requestTransformations[0].transformationTemplate.headers", ":path", String.format("{\"text\":\"%s\"}", transformPath));
        } else {
            // 新的使用方式
            // 例如rewrite_regex: /anything/(.*)/(.*) $.action.target:/$2/$1
            Matcher matcher = Pattern.compile("\\$\\d").matcher(rg.getValue("$.action.target"));
            int regexCount = 0;
            while (matcher.find()) {
                regexCount++;
            }
            String original = rg.getValue("$.action.rewrite_regex");
            String target = transformPath.replaceAll("(\\$\\d)", "{{$1}}");
            for (int i = 1; i <= regexCount; i++) {
                String key = "$" + i;
                String value = String.format("{\"header\":\":path\",\"regex\":\"%s\",\"subgroup\":%s}", original, i);
                ret.createOrUpdateJson("$.transformation.requestTransformations[0].transformationTemplate.extractors", key, value);
            }
            ret.createOrUpdateJson("$.transformation.requestTransformations[0].transformationTemplate.headers", ":path", String.format("{\"text\":\"%s\"}", target));
        }
        return ret.jsonString();
    }
}
