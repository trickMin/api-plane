package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
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
public abstract class AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {

    @Autowired
    protected EditorContext editorContext;

    @Autowired
    protected List<SchemaProcessor> processorList;

    protected SchemaProcessor getProcessor(String name) {
        if (CollectionUtils.isEmpty(processorList)) return null;
        for (SchemaProcessor item : processorList) {
            if (name.equalsIgnoreCase(item.getName())) return item;
        }
        return null;
    }

    protected String getApiName(ServiceInfo serviceInfo) {
        return serviceInfo.getApiName();
    }

    protected String getServiceName(ServiceInfo serviceInfo) {
        return serviceInfo.getServiceName();
    }

    protected String createMatch(ResourceGenerator rg, ServiceInfo info, String xUserId) {
        ResourceGenerator match = ResourceGenerator.newInstance("[{}]", ResourceType.JSON, editorContext);
        // 添加默认的字段
        match.createOrUpdateJson("$[0]", "uri", String.format("{\"regex\":\"(?:%s.*)\"}", info.getUri()));
        match.createOrUpdateJson("$[0]", "method", String.format("{\"regex\":\"%s\"}", info.getMethod()));
        match.createOrUpdateJson("$[0]", "headers", String.format("{\":authority\":{\"regex\":\"%s\"}}", info.getHosts()));

        // 增加租户信息
        if (StringUtils.isNotBlank(xUserId)) {
            match.createOrUpdateJson("$[0].headers", "x_user_id", String.format("{\"regex\":\"%s\"}", xUserId));
        }

        if (rg.contain("$.matcher")) {
            int length = rg.getValue("$.matcher.length()");
            for (int i = 0; i < length; i++) {
                String sourceType = rg.getValue(String.format("$.matcher[%d].source_type", i));
                String leftValue = rg.getValue(String.format("$.matcher[%d].left_value", i));
                String op = rg.getValue(String.format("$.matcher[%d].op", i));
                String rightValue = rg.getValue(String.format("$.matcher[%d].right_value", i));

                switch (sourceType) {
                    case "Args":
                        match.createOrUpdateJson("$[0]", "queryParams", String.format("{\"%s\":{\"regex\":\"%s\"}}", leftValue, getRegexByOp(op, rightValue)));
                        break;
                    case "Header":
                        match.createOrUpdateJson("$[0].headers", leftValue, String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
                        break;
                    case "Cookie":
                        match.createOrUpdateJson("$[0].headers", "Cookie", String.format("{\"regex\":\".*(?:;|^)%s=%s(?:;|$).*\"}", leftValue, getRegexByOp(op, rightValue)));
                        break;
                    case "User-Agent":
                        match.createOrUpdateJson("$[0].headers", "User-Agent", String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
                        break;
                    case "URI":
                        match.createOrUpdateJson("$[0].headers", ":path", String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
                        break;
                    case "Host":
                        match.createOrUpdateJson("$[0].headers", ":authority", String.format("{\"regex\":\"%s\"}", getRegexByOp(op, rightValue)));
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
            case "exact":
            case "=":
                return String.format("%s", escapeExprSpecialWord(value));
            case "!=":
                return String.format("((?!%s).)*", escapeExprSpecialWord(value));
            case "regex":
            case "≈":
                return escapeBackSlash(value);
            case "prefix":
            case "startsWith":
                return String.format("%s.*", escapeExprSpecialWord(value));
            case "endsWith":
                return String.format(".*%s", escapeExprSpecialWord(value));
            case "nonRegex":
            case "!≈":
                return String.format("((?!%s).)*", escapeBackSlash(value));
            default:
                throw new ApiPlaneException("Unsupported op :[" + op + "]");
        }
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

    protected String getAndDeleteXUserId(ResourceGenerator rg) {
        if (rg.contain("$.x_user_id")) {
            String xUserId = rg.getValue("$.x_user_id");
            rg.removeElement("$.x_user_id");
            return xUserId;
        }
        return null;
    }

    protected Integer getPriority(ResourceGenerator rg) {
        return rg.getValue("$.priority", Integer.class);
    }
}
