package com.netease.cloud.nsf.core.plugin;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/20
 **/
public abstract class AbstractYxSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Autowired
    private EditorContext editorContext;

    protected String getDefaultRoute(ServiceInfo serviceInfo) {
        ResourceGenerator rg = ResourceGenerator.newInstance(serviceInfo.getRoute(), ResourceType.YAML, editorContext);
        Object route = rg.getValue("$.route");
        return ResourceGenerator.obj2json(route, editorContext);
    }

    protected String getDefaultMatch(ServiceInfo serviceInfo) {
        ResourceGenerator rg = ResourceGenerator.newInstance(serviceInfo.getMatch(), ResourceType.YAML, editorContext);
        Object route = rg.getValue("$.match");
        return ResourceGenerator.obj2json(route, editorContext);
    }

    protected String createMatch(ResourceGenerator rg, ServiceInfo info) {
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

            if (match.contain("$[0].headers")) {
                match.createOrUpdateJson("$[0].headers", "Cookie", String.format("{\"regex\":\".*(?:;|^)%s=%s(?:;|$).*\"}", leftValue, getRegexByOp(op, rightValue)));
            } else {
                match.createOrUpdateJson("$[0]", "headers", String.format("{\"Cookie\":{\"regex\":\".*(?:;|^)%s=%s(?:;|$).*\"}}", leftValue, getRegexByOp(op, rightValue)));
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
        // todo： 不支持Args
        return match.jsonString();
    }

    protected String getRegexByOp(String op, String value) {
        switch (op) {
            case "=":
                return String.format("%s", escapeExprSpecialWord(value));
            case "regex":
                return value;
            case "startsWith":
                return String.format("%s.*", escapeExprSpecialWord(value));
            case "endsWith":
                return String.format(".*%s", escapeExprSpecialWord(value));
            case "nonRegex":
                return String.format("((?!%s).)*", escapeExprSpecialWord(value));
            default:
                throw new ApiPlaneException("Unsupported op.");
        }
    }

    protected Integer getPort(List<Endpoint> endpoints, String targetHost) {
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

    protected String escapeExprSpecialWord(String keyword) {
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
