package com.netease.cloud.nsf.core.plugin;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.plugin.meta.Matcher;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.netease.cloud.nsf.util.PluginConst.*;


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
    private EditorContext editorContext;

    @Override
    public String getName() {
        return "RouteProcessor";
    }

    @Override
    public String process(String plugin) {
        List<HTTPRoute> ret = new ArrayList<>();
        ResourceGenerator total = ResourceGenerator.newInstance(plugin, ResourceType.JSON, editorContext);
        // 将路由plugin细分，例如rewrite部分,redirect部分
        List plugins = total.getValue("$.rule");
        plugins.stream().forEach(innerPlugin -> {
            ResourceGenerator rg = ResourceGenerator.newInstance(innerPlugin, ResourceType.OBJECT, editorContext);
            String innerType = rg.getValue("$.name");
            switch (innerType) {
                case "rewrite": {
                    ret.add(createRewrite(rg));
                    break;
                }
                case "redirect": {
                    ret.add(createRedirect(rg));
                    break;
                }
                case "return": {
                    ret.add(createReturn(rg));
                    break;
                }
                case "pass_proxy": {
                    ret.add(createPassProxy(rg));
                    break;
                }
                default:
                    throw new ApiPlaneException("Unsupported inner routing plugin types.");
            }
        });
        return ResourceGenerator.newInstance(ret, ResourceType.OBJECT, editorContext).jsonString();
    }

    private HTTPRoute createPassProxy(ResourceGenerator rg) {
        HTTPMatchRequest match = createMatch(rg);
        return new HTTPRouteBuilder().withName(TEMPLATE_APINAME).withMatch(match).withRoute(new HTTPRouteDestinationBuilder()
                .withDestination(new DestinationBuilder().withHost(TEMPLATE_URI)
                        .withPort(new PortSelectorBuilder().withPort(new NumberPortBuilder().withNumber(8080).build()).build()).withSubset(TEMPLATE_SUBSET).build()).build()).build();
    }

    private HTTPRoute createReturn(ResourceGenerator rg) {
        HTTPMatchRequest match = createMatch(rg);
        HttpReturn httpReturn = new HttpReturnBuilder().withCode(rg.getValue("$.action.code"))
                .withBody(new DataSourceBuilder().withInlineString(rg.getValue("$.action.body")).build()).build();
        return new HTTPRouteBuilder().withName(TEMPLATE_APINAME).withMatch(match).withReturn(httpReturn).build();
    }

    private HTTPRoute createRedirect(ResourceGenerator rg) {
        HTTPMatchRequest match = createMatch(rg);
        HTTPRedirect redirect = new HTTPRedirectBuilder().withUri(rg.getValue("$.action.target")).build();
        return new HTTPRouteBuilder().withName(TEMPLATE_APINAME).withMatch(match).withRedirect(redirect).build();
    }

    private HTTPRoute createRewrite(ResourceGenerator rg) {
        HTTPMatchRequest match = createMatch(rg);
        HTTPRewrite rewrite = new HTTPRewriteBuilder().withUri(rg.getValue("$.action.target")).build();
        return new HTTPRouteBuilder().withName(TEMPLATE_APINAME).withMatch(match).withRewrite(rewrite).build();
    }

    private HTTPMatchRequest createMatch(ResourceGenerator rg) {
        HTTPMatchRequest matchRequest = new HTTPMatchRequest();
        // 处理source_type = 'Header'的matcher
        List headers = rg.getValue("$.matcher[?(@.source_type == 'Header')]");
        if (!CollectionUtils.isEmpty(headers)) {
            Matcher header = ResourceGenerator.newInstance(headers.get(0), ResourceType.OBJECT, editorContext).object(Matcher.class);
            matchRequest.setHeaders(new HashMap() {{
                put(header.getLeftValue(), createMatchType(header));
            }});
            matchRequest.setUri(new StringMatchBuilder().withMatchType(new RegexMatchTypeBuilder().withRegex(TEMPLATE_URI).build()).build());
        }
        // 处理source_type = 'URI'的matcher
        List uris = rg.getValue("$.matcher[?(@.source_type == 'URI')]");
        if (!CollectionUtils.isEmpty(uris)) {
            Matcher uri = ResourceGenerator.newInstance(uris.get(0), ResourceType.OBJECT, editorContext).object(Matcher.class);
            StringMatch tmp = createMatchType(uri);
            if (tmp.getMatchType() instanceof RegexMatchType) {
                String regex = String.format("(?:%s)|(?:%s)", TEMPLATE_URI, ((RegexMatchType) tmp.getMatchType()).getRegex());
                matchRequest.setUri(new StringMatchBuilder().withMatchType(new RegexMatchTypeBuilder().withRegex(regex).build()).build());
            } else {
                matchRequest.setUri(tmp);
            }
        }
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
