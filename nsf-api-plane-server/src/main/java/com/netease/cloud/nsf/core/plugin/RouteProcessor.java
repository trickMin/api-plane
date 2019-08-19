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
 *
 * todo: 路由path 结合插件path
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/7
 **/
@Component
public class RouteProcessor implements SchemaProcessor<ServiceInfo> {
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
        ret.createOrUpdateJson("$", "name", info.getApiName());
        ret.createOrUpdateJson("$", "route", createDefaultRoute(info));
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
        ret.createOrUpdateJson("$", "route", createDefaultRoute(info));
        return ret.jsonString();
    }

    //todo: proxyUri有多个
    private String createDefaultRoute(ServiceInfo info) {
        List<Endpoint> endpoints = istioHttpClient.getEndpointList();
        String host = info.getApi().getProxyUris().get(0);
        Integer port = getPort(endpoints, host);
        return String.format("[{\"destination\":{\"host\":\"%s\",\"port\":{\"number\":%d},\"subset\":\"%s\"},\"weight\":100}]",
                host, port, info.getSubset());
    }

    private String createMatch(ResourceGenerator rg, ServiceInfo info) {
        ResourceGenerator match = ResourceGenerator.newInstance("[{}]", ResourceType.JSON, editorContext);
        // 添加默认的字段
        match.createOrUpdateJson("$[0]", "uri", String.format("{\"regex\":\"(?:%s.*)\"}", info.getUri()));
        match.createOrUpdateJson("$[0]", "method", String.format("{\"regex\":\"%s\"}", info.getMethod()));

        // 处理source_type = 'URI'的matcher
        List uris = rg.getValue("$.matcher[?(@.source_type == 'URI')]");
        if (!CollectionUtils.isEmpty(uris)) {
            ResourceGenerator uri = ResourceGenerator.newInstance(uris.get(0), ResourceType.OBJECT, editorContext);
            String op = uri.getValue("$.op");
            String rightValue = uri.getValue("$.right_value");

            match.createOrUpdateJson("$[0]", "uri", String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
        }
        // 处理source_type = 'Header'的matcher
        List headers = rg.getValue("$.matcher[?(@.source_type == 'Header')]");
        if (!CollectionUtils.isEmpty(headers)) {
            ResourceGenerator header = ResourceGenerator.newInstance(headers.get(0), ResourceType.OBJECT, editorContext);
            String op = header.getValue("$.op");
            String leftValue = header.getValue("$.left_value");
            String rightValue = header.getValue("$.right_value");

            if (match.contain("$[0].headers")) {
                match.createOrUpdateJson("$[0].headers", leftValue, String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
            } else {
                match.createOrUpdateJson("$[0]", "headers", String.format("{\"%s\":{\"regex\":\"%s\"}}", leftValue, getRegexByOp(op, rightValue)));
            }
        }
        // 处理source_type = 'Cookie'的matcher
        List cookies = rg.getValue("$.matcher[?(@.source_type == 'Cookie')]");
        if (!CollectionUtils.isEmpty(cookies)) {
            ResourceGenerator cookie = ResourceGenerator.newInstance(cookies.get(0), ResourceType.OBJECT, editorContext);
            String op = cookie.getValue("$.op");
            String leftValue = cookie.getValue("$.left_value");
            String rightValue = cookie.getValue("$.right_value");

            //todo: 可能需要url encode
            if (match.contain("$[0].headers")) {
                match.createOrUpdateJson("$[0].headers", "Cookie", String.format("{\"regex\":\"%s=%s(?:;|$)\"}", leftValue, getRegexByOp(op, rightValue)));
            } else {
                match.createOrUpdateJson("$[0]", "headers", String.format("{\"Cookie\":{\"regex\":\"%s=%s(?:;|$)\"}}", leftValue, getRegexByOp(op, rightValue)));
            }
        }
        // 处理source_type = 'User-Agent'的matcher
        List agents = rg.getValue("$.matcher[?(@.source_type == 'User-Agent')]");
        if (!CollectionUtils.isEmpty(agents)) {
            ResourceGenerator agent = ResourceGenerator.newInstance(agents.get(0), ResourceType.OBJECT, editorContext);
            String op = agent.getValue("$.op");
            String rightValue = agent.getValue("$.right_value");

            if (match.contain("$[0].headers")) {
                match.createOrUpdateJson("$[0].headers", "User-Agent", String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
            } else {
                match.createOrUpdateJson("$[0]", "headers", String.format("{\"User-Agent\":{\"regex\":\"%s\"}}", getRegexByOp(op, rightValue)));
            }
        }
        // 处理source_type = 'Host'的matcher
        List hosts = rg.getValue("$.matcher[?(@.source_type == 'Host')]");
        if (!CollectionUtils.isEmpty(hosts)) {
            ResourceGenerator agent = ResourceGenerator.newInstance(agents.get(0), ResourceType.OBJECT, editorContext);
            String op = agent.getValue("$.op");
            String rightValue = agent.getValue("$.right_value");

            if (match.contain("$[0].headers")) {
                match.createOrUpdateJson("$[0].headers", "Host", String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
            } else {
                match.createOrUpdateJson("$[0]", "headers", String.format("{\"Host\":{\"regex\":\"%s\"}}", getRegexByOp(op, rightValue)));
            }
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
            case "endsWith":
                return String.format("^.*%s$", escapeExprSpecialWord(value));
            case "nonRegex":
                return String.format("^((?!%s).)*$", escapeExprSpecialWord(value));
            default:
                throw new ApiPlaneException("Unsupported op.");
        }
    }

    private Integer getPort(List<Endpoint> endpoints, String targetHost) {
        if (CollectionUtils.isEmpty(endpoints) || StringUtils.isBlank(targetHost)) {
            throw new ApiPlaneException("Get port by targetHost fail. param cant be null.");
        }
        for (Endpoint endpoint : endpoints) {
            if (targetHost.equals(endpoint.getHostname())) {
                return endpoint.getPort();
            }
        }
        throw new ApiPlaneException(String.format("Target endpoint %s does not exist", targetHost));
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
