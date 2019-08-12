package com.netease.cloud.nsf.core.plugin;

import com.jayway.jsonpath.Criteria;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.IstioHttpClient;
import com.netease.cloud.nsf.core.plugin.meta.Matcher;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.sun.javafx.binding.StringFormatter;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;


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
    public String process(String plugin, ServiceInfo serviceInfo) {
        Map<String, HTTPRoute> pluginMap = new LinkedHashMap<>();
        // 返回前Consumer, 如果需要在返回前对结果进行最后修改，可以添加Consumer到这里
        List<Consumer<ResourceGenerator>> consumerBeforeReturn = new ArrayList<>();

        ResourceGenerator total = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        // 将路由plugin细分，例如rewrite部分,redirect部分
        List plugins = total.getValue("$.rule");
        plugins.stream().forEach(innerPlugin -> {
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

        // 判断rewrite插件是否存在pass_proxy插件，有则用pass_proxy作为route
        if (pluginMap.containsKey("rewrite")) {
            if (pluginMap.containsKey("pass_proxy")) {
                pluginMap.get("rewrite").setRoute(pluginMap.get("pass_proxy").getRoute());
            } else {
                // 没有则用当前服务作为route,
                String routeJson = StringFormatter.format("[{\"destination\":{\"host\":\"%s\",\"port\":{\"number\":%s},\"subset\":\"%s\"}}]"
                        , serviceInfo.getUri(), serviceInfo.getPort(), serviceInfo.getSubset()).getValue();
                Object routeObject = ResourceGenerator.json2obj(routeJson, List.class, editorContext);
                consumerBeforeReturn.add(generator -> generator.createOrUpdateValue("$[?]", "route", routeObject, Criteria.where("rewrite").exists(true)));
            }
        }

        // 最后返回的result
        ResourceGenerator result = ResourceGenerator.newInstance(pluginMap.values(), ResourceType.OBJECT, editorContext);
        // 在返回前使用Consumer过滤一遍， 需要最后进行json修改可以在这里进行
        consumerBeforeReturn.forEach(consumer -> consumer.accept(result));
        return result.yamlString();
    }

    private HTTPRoute createPassProxy(ResourceGenerator rg, ServiceInfo info) {
        HTTPMatchRequest match = createMatch(rg, info);
        return new HTTPRouteBuilder().withName(info.getApiName()).withMatch(match).withRoute(createProxyRoute(rg, info)).build();
    }

    private HTTPRoute createReturn(ResourceGenerator rg, ServiceInfo info) {
        HTTPMatchRequest match = createMatch(rg, info);
        HttpReturn httpReturn = new HttpReturnBuilder().withCode(rg.getValue("$.action.code"))
                .withBody(new DataSourceBuilder().withInlineString(rg.getValue("$.action.body")).build()).build();
        return new HTTPRouteBuilder().withName(info.getApiName()).withMatch(match).withReturn(httpReturn).build();
    }

    private HTTPRoute createRedirect(ResourceGenerator rg, ServiceInfo info) {
        HTTPMatchRequest match = createMatch(rg, info);
        HTTPRedirect redirect = new HTTPRedirectBuilder().withUri(rg.getValue("$.action.target")).build();
        return new HTTPRouteBuilder().withName(info.getApiName()).withMatch(match).withRedirect(redirect).build();
    }

    private HTTPRoute createRewrite(ResourceGenerator rg, ServiceInfo info) {
        HTTPMatchRequest match = createMatch(rg, info);
        HTTPRewrite rewrite = new HTTPRewriteBuilder().withUri(rg.getValue("$.action.target")).build();
        return new HTTPRouteBuilder().withName(info.getApiName()).withMatch(match).withRewrite(rewrite).build();
    }

    private HTTPMatchRequest createMatch(ResourceGenerator rg, ServiceInfo info) {
        HTTPMatchRequest matchRequest = new HTTPMatchRequest();
        // 处理source_type = 'Header'的matcher
        List headers = rg.getValue("$.matcher[?(@.source_type == 'Header')]");
        if (!CollectionUtils.isEmpty(headers)) {
            Matcher header = ResourceGenerator.newInstance(headers.get(0), ResourceType.OBJECT, editorContext).object(Matcher.class);
            matchRequest.setHeaders(new HashMap() {{
                put(header.getLeftValue(), createMatchType(header));
            }});
            matchRequest.setUri(new StringMatchBuilder().withMatchType(new RegexMatchTypeBuilder().withRegex(info.getUri()).build()).build());
        }
        // 处理source_type = 'URI'的matcher
        List uris = rg.getValue("$.matcher[?(@.source_type == 'URI')]");
        if (!CollectionUtils.isEmpty(uris)) {
            Matcher uri = ResourceGenerator.newInstance(uris.get(0), ResourceType.OBJECT, editorContext).object(Matcher.class);
            StringMatch tmp = createMatchType(uri);
            if (tmp.getMatchType() instanceof RegexMatchType) {
                String regex = String.format("(?:%s)|(?:%s)", info.getUri(), ((RegexMatchType) tmp.getMatchType()).getRegex());
                matchRequest.setUri(new StringMatchBuilder().withMatchType(new RegexMatchTypeBuilder().withRegex(regex).build()).build());
            } else {
                matchRequest.setUri(tmp);
            }
        }
        // match method
        matchRequest.setMethod(new StringMatchBuilder().withMatchType(new ExactMatchTypeBuilder().withExact(info.getMethod()).build()).build());
        // todo: Cookie, User-Agent, Args, Host, 并且现在的模型似乎不支持Args，需要更新?
        return matchRequest;
    }

    private StringMatch createMatchType(Matcher matcher) {
        StringMatch stringMatch = null;
        switch (matcher.getOp()) {
            case "=": {
                String regex = String.format("^%s$", escapeExprSpecialWord(matcher.getRightValue()));
                RegexMatchType matchType = new RegexMatchTypeBuilder().withRegex(regex).build();
                stringMatch = new StringMatchBuilder().withRegexMatchType(matchType).build();
                break;
            }
            case "regex": {
                RegexMatchType matchType = new RegexMatchTypeBuilder().withRegex(matcher.getRightValue()).build();
                stringMatch = new StringMatchBuilder().withRegexMatchType(matchType).build();
                break;
            }
            case "startsWith": {
                String regex = String.format("^%s.*$", escapeExprSpecialWord(matcher.getRightValue()));
                RegexMatchType matchType = new RegexMatchTypeBuilder().withRegex(regex).build();
                stringMatch = new StringMatchBuilder().withRegexMatchType(matchType).build();
                break;
            }
            default:
                throw new ApiPlaneException("Unsupported op.");
        }
        return stringMatch;
    }

    private List<HTTPRouteDestination> createProxyRoute(ResourceGenerator rg, ServiceInfo info) {
        List<HTTPRouteDestination> ret = new ArrayList<>();
        List<Object> proxyTarget = rg.getValue("$.action.pass_proxy_target[*]");
        proxyTarget.stream().forEach(proxy -> {
            ResourceGenerator prg = ResourceGenerator.newInstance(proxy, ResourceType.OBJECT, editorContext);
            HTTPRouteDestination destination = new HTTPRouteDestinationBuilder()
                    .withDestination(new DestinationBuilder().withHost(prg.getValue("$.url"))
                            .withPort(new PortSelectorBuilder().withPort(new NumberPortBuilder().withNumber(8080).build()).build()).withSubset(info.getSubset()).build()).build();
            ret.add(destination);
        });
        return ret;
    }

    private String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }
}
