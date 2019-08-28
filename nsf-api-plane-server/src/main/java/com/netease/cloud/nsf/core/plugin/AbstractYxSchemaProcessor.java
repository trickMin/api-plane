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
    protected EditorContext editorContext;

    protected String getApiName(ServiceInfo serviceInfo) {
        return serviceInfo.getApi().getName();
    }

    protected String getServiceName(ServiceInfo serviceInfo) {
        return serviceInfo.getApi().getService();
    }

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

        if (rg.contain("$.matcher")) {
            int length = rg.getValue("$.matcher.length()");
            for (int i = 0; i < length; i++) {
                String sourceType = rg.getValue(String.format("$.matcher[%d].source_type", i));
                String leftValue = rg.getValue(String.format("$.matcher[%d].left_value", i));
                String op = rg.getValue(String.format("$.matcher[%d].op", i));
                String rightValue = rg.getValue(String.format("$.matcher[%d].right_value", i));

                switch (sourceType) {
                    case "URI":
                        match.createOrUpdateJson("$[0]", "uri", String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
                        break;
                    case "Args":
                        match.createOrUpdateJson("$[0]", "queryParams", String.format("{\"%s\":{\"regex\":\"%s\"}}", leftValue, getRegexByOp(op, rightValue)));
                        break;
                    case "Header":
                        if (match.contain("$[0].headers")) {
                            match.createOrUpdateJson("$[0].headers", leftValue, String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
                        } else {
                            match.createOrUpdateJson("$[0]", "headers", String.format("{\"%s\":{\"regex\":\"%s\"}}", leftValue, getRegexByOp(op, rightValue)));
                        }
                        break;
                    case "Cookie":
                        if (match.contain("$[0].headers")) {
                            match.createOrUpdateJson("$[0].headers", "Cookie", String.format("{\"regex\":\".*(?:;|^)%s=%s(?:;|$).*\"}", leftValue, getRegexByOp(op, rightValue)));
                        } else {
                            match.createOrUpdateJson("$[0]", "headers", String.format("{\"Cookie\":{\"regex\":\".*(?:;|^)%s=%s(?:;|$).*\"}}", leftValue, getRegexByOp(op, rightValue)));
                        }
                        break;
                    case "User-Agent":
                        if (match.contain("$[0].headers")) {
                            match.createOrUpdateJson("$[0].headers", "User-Agent", String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
                        } else {
                            match.createOrUpdateJson("$[0]", "headers", String.format("{\"User-Agent\":{\"regex\":\"%s\"}}", getRegexByOp(op, rightValue)));
                        }
                        break;
                    case "Host":
                        if (match.contain("$[0].headers")) {
                            match.createOrUpdateJson("$[0].headers", ":authority", String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
                        } else {
                            match.createOrUpdateJson("$[0]", "headers", String.format("{\":authority\":{\"regex\":\"%s\"}}", getRegexByOp(op, rightValue)));
                        }
                        break;
                    default:
                        throw new ApiPlaneException("Unsupported match : " + sourceType);
                }
            }
        }
        return match.jsonString();
    }

    protected String getRegexByOp(String op, String value) {
        switch (op) {
            case "=":
                return String.format("%s", escapeExprSpecialWord(value));
            case "!=":
                return String.format("((?!%s).)*", escapeExprSpecialWord(value));
            case "regex":
                return value;
            case "startsWith":
                return String.format("%s.*", escapeExprSpecialWord(value));
            case "endsWith":
                return String.format(".*%s", escapeExprSpecialWord(value));
            case "nonRegex":
                return String.format("((?!%s).)*", value);
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

    /**
     * 将一个普通expression作为regex表达式，转移其中所有特殊字符，并填充到json中时需要转义,
     * 例如 . 转义为 \\\\. 第一个反斜杠转义是不作为正则表达式中的特殊字符.第二个反斜杆是在json中特殊字符需要转义
     *
     * @param keyword
     * @return
     */
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

    /**
     * 将一个正则expression填充到json中时，需要转义\
     *
     * @param keyword
     * @return
     */
    protected String escapeBackSlash(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            keyword = keyword.replaceAll("\\\\", "\\\\\\\\");
        }
        return keyword;
    }

    protected void appendExtra(ResourceGenerator gen) {
        gen.createOrUpdateValue("$[*]", "extra", "<@indent count=4>${t_virtual_service_extra}</@indent>");
    }
}
