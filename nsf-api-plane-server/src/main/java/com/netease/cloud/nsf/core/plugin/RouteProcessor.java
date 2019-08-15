package com.netease.cloud.nsf.core.plugin;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.IstioHttpClient;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;


/**
 * 路由插件的转换processor
 * <p>
 * 因为路由插件比较复杂，所以总体方案是
 * plugin内容 转成 java model 再转出 json
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/7
 **/
@Component
public class RouteProcessor implements SchemaProcessor {
    @Autowired
    private IstioHttpClient istioHttpClient;

    @Autowired
    private EditorContext editorContext;

    @Override
    public String getName() {
        return "RouteProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        Map<String, String> pluginMap = new LinkedHashMap<>();
        ResourceGenerator total = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        // 将路由plugin细分，例如rewrite部分,redirect部分
        List<Object> plugins = total.getValue("$.rule");
        plugins.forEach(innerPlugin -> {
            ResourceGenerator rg = ResourceGenerator.newInstance(innerPlugin, ResourceType.OBJECT, editorContext);
            String innerType = rg.getValue("$.name");
            switch (innerType) {
                case "rewrite": {
                    pluginMap.put(innerType, createRewrite(rg, serviceInfo));
                    break;
                }
                case "redirect": {
                    pluginMap.put(innerType, createRedirect(rg, serviceInfo));
                    break;
                }
                case "return": {
                    pluginMap.put(innerType, createReturn(rg, serviceInfo));
                    break;
                }
                case "pass_proxy": {
                    pluginMap.put(innerType, createPassProxy(rg, serviceInfo));
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
        pluginMap.values().forEach(o -> result.addJsonElement("$", o));

        FragmentHolder holder = new FragmentHolder();
        holder.setVirtualServiceFragment(result.yamlString());
        return holder;
    }

    private String createPassProxy(ResourceGenerator rg, ServiceInfo info) {
        List<Endpoint> endpoints = istioHttpClient.getEndpointList();

        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info));
        ret.createOrUpdateJson("$", "route", "[]");
        ret.createOrUpdateJson("$", "name", info.getApiName());

        int length = rg.getValue("$.action.pass_proxy_target.length()");
        for (int i = 0; i < length; i++) {
            String targetHost = rg.getValue(String.format("$.action.pass_proxy_target[%d].url", i));
            Integer weight = rg.getValue(String.format("$.action.pass_proxy_target[%d].weight", i));
            // 根据host查找host的port
            endpoints.stream().filter(endpoint -> targetHost.equals(endpoint.getHostname())).findAny().ifPresent(
                    endpoint ->
                            ret.addJsonElement("$.route",
                                    String.format("{\"destination\":{\"host\":\"%s\",\"port\":{\"number\":%d},\"subset\":\"%s\"},\"weight\":%d}",
                                            targetHost, endpoint.getPort(), info.getSubset(), weight))
            );
        }
        return ret.jsonString();
    }

    private String createReturn(ResourceGenerator rg, ServiceInfo info) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info));
        ret.createOrUpdateJson("$", "return",
                String.format("{\"body\":{\"inlineString\":\"%s\"},\"code\":%s}", rg.getValue("$.action.body"), rg.getValue("$.action.code")));
        ret.createOrUpdateJson("$", "name", info.getApiName());
//        ret.createOrUpdateJson("$", "route", "[{\"destination\":{\"host\":\"productpage.default.svc.cluster.local\",\"port\":{\"number\":9080},\"subset\":\"service-zero-plane-istio-test-gateway-yx\"},\"weight\":100}]");
        return ret.jsonString();
    }

    private String createRedirect(ResourceGenerator rg, ServiceInfo info) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info));
        ret.createOrUpdateJson("$", "redirect", String.format("{\"uri\":\"%s\"}", rg.getValue("$.action.target", String.class)));
        ret.createOrUpdateJson("$", "name", info.getApiName());
        return ret.jsonString();
    }

    private String createRewrite(ResourceGenerator rg, ServiceInfo info) {
        ResourceGenerator ret = ResourceGenerator.newInstance("{}", ResourceType.JSON, editorContext);
        ret.createOrUpdateJson("$", "match", createMatch(rg, info));
        ret.createOrUpdateJson("$", "requestTransform", String.format("{\"new\":{\"path\":\"%s\"},\"orignal\":{\"path\":\"%s\"}}"
                , rg.getValue("$.action.target", String.class), rg.getValue("$.action.rewrite_regex")));
        ret.createOrUpdateJson("$", "name", info.getApiName());
//        ret.createOrUpdateJson("$", "route", "[{\"destination\":{\"host\":\"productpage.default.svc.cluster.local\",\"port\":{\"number\":9080},\"subset\":\"service-zero-plane-istio-test-gateway-yx\"},\"weight\":100}]");
        return ret.jsonString();
    }

    private String createMatch(ResourceGenerator rg, ServiceInfo info) {
        ResourceGenerator match = ResourceGenerator.newInstance("[{}]", ResourceType.JSON, editorContext);
        // 添加默认的字段
        match.createOrUpdateJson("$[0]", "uri", String.format("{\"regex\":\"(?:%s.*)\"}", info.getUri()));
        match.createOrUpdateJson("$[0]", "method", String.format("{\"regex\":\"%s\"}", info.getMethod()));

        // 处理source_type = 'Header'的matcher
        List headers = rg.getValue("$.matcher[?(@.source_type == 'Header')]");
        if (!CollectionUtils.isEmpty(headers)) {
            ResourceGenerator header = ResourceGenerator.newInstance(headers.get(0), ResourceType.OBJECT, editorContext);
            String op = header.getValue("$.op");
            String leftValue = header.getValue("$.left_value");
            String rightValue = header.getValue("$.right_value");

            match.createOrUpdateJson("$[0]", "headers", String.format("{\"%s\":{\"regex\":\"%s\"}}", leftValue, getRegexByOp(op, rightValue)));
        }
        // 处理source_type = 'URI'的matcher
        List uris = rg.getValue("$.matcher[?(@.source_type == 'URI')]");
        if (!CollectionUtils.isEmpty(uris)) {
            ResourceGenerator uri = ResourceGenerator.newInstance(uris.get(0), ResourceType.OBJECT, editorContext);
            String op = uri.getValue("$.op");
            String rightValue = uri.getValue("$.right_value");

            match.createOrUpdateJson("$[0]", "uri", String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
        }
        // todo: Cookie, User-Agent, Args, Host, 并且现在的模型似乎不支持Args，需要更新?
        return match.jsonString();
    }

    private String getRegexByOp(String op, String value) {
        switch (op) {
            case "=":
                return String.format("^%s$", escapeExprSpecialWord(value));
            case "regex":
                return value;
            case "startsWith":
                return String.format("^%s.*$", escapeExprSpecialWord(value));
            default:
                throw new ApiPlaneException("Unsupported op.");
        }
    }

    private String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\\\" + key);
                }
            }
        }
        return keyword;
    }
}
