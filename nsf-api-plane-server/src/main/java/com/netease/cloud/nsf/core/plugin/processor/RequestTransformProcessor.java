package com.netease.cloud.nsf.core.plugin.processor;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.plugin.FragmentHolder;
import com.netease.cloud.nsf.core.plugin.FragmentTypeEnum;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.ServiceInfo;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/11/18
 **/
@Component
public class RequestTransformProcessor extends AbstractSchemaProcessor implements SchemaProcessor<ServiceInfo> {
    @Override
    public String getName() {
        return "RequestTransformProcessor";
    }

    @Override
    public FragmentHolder process(String plugin, ServiceInfo serviceInfo) {
        ResourceGenerator total = ResourceGenerator.newInstance(plugin);
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"transformation\":{\"requestTransformations\":[]}}");
        int matchLen = total.getValue("$.matcher.length()");
        for (int i = 0; i < matchLen; i++) {
            ResourceGenerator conditionRg = ResourceGenerator.newInstance(total.getValue(String.format("$.matcher[%s].content", i)), ResourceType.OBJECT);
            ResourceGenerator actionRg = ResourceGenerator.newInstance(total.getValue(String.format("$.matcher[%s].content", i)), ResourceType.OBJECT);
            builder.addJsonElement("$.transformation.requestTransformations", "{}");
            builder.createOrUpdateJson(String.format("$.transformation.requestTransformations[%s]", i), "conditions", createConditions(conditionRg));
            builder.createOrUpdateJson(String.format("$.transformation.requestTransformations[%s]", i), "transformationTemplate", createTransformationTemplate(actionRg));
        }

        FragmentHolder fragmentHolder = new FragmentHolder();
        FragmentWrapper wrapper = new FragmentWrapper.Builder()
                .withFragmentType(FragmentTypeEnum.VS_API)
                .withResourceType(K8sResourceEnum.VirtualService)
                .withContent(builder.yamlString())
                .build();
        fragmentHolder.setVirtualServiceFragment(wrapper);
        return fragmentHolder;
    }

    /**
     * before transform:
     * {
     * "condition": [
     * {
     * "target": "header",
     * "left_value": "Host",
     * "right_operator": "=",
     * "right_value": "baidu.com"
     * }
     * ]
     * }
     * <p>
     * after transform:
     * [
     * {
     * "headers": {
     * "Host": {
     * "regex": "baidu.com"
     * }
     * }
     * }
     * ]
     */
    private String createConditions(ResourceGenerator source) {
        ResourceGenerator builder = ResourceGenerator.newInstance("[]");
        int conditionLen = source.getValue("$.condition.length()");
        for (int i = 0; i < conditionLen; i++) {
            String target = source.getValue(String.format("$.condition[%s].target", i));
            String leftValue = source.getValue(String.format("$.condition[%s].left_value", i));
            String rightOperator = source.getValue(String.format("$.condition[%s].right_operator", i));
            String rightValue = source.getValue(String.format("$.condition[%s].right_value", i));

            switch (target) {
                case "header":
                    ResourceGenerator headerBuilder = ResourceGenerator.newInstance("{\"headers\":{}}");
                    String match = String.format("{\"regex\":\"%s\"}", getRegexByOp(rightOperator, rightValue));
                    headerBuilder.createOrUpdateJson("$.headers", leftValue, match);
                    builder.addJsonElement("$", headerBuilder.jsonString());
                    break;
                case "uri":
                case "host":
                case "userAgent":
                case "referer":
                case "method":
                case "args":
                case "cookie":
                    //todo:
                    break;
                default:
                    throw new ApiPlaneException("Unsupported target : " + target);
            }
        }
        return builder.jsonString();
    }

    /**
     * before transform:
     * {
     * "action": [
     * {
     * "operation": "append",
     * "target": "header",
     * "expression": "abc"
     * }
     * ]
     * }
     * <p>
     * after transform:
     * {
     * "transformationTemplate": {
     * "extractors": {
     * "$1": {
     * "header": ":path",
     * "regex": " /rewrite/(.*)",
     * "subgroup": 1
     * }
     * },
     * "headers": {
     * ":path": {
     * "text": "/{{$1}}"
     * }
     * }
     * }
     * }
     */
    private String createTransformationTemplate(ResourceGenerator source) {
        ResourceGenerator builder = ResourceGenerator.newInstance("{\"extractors\":{},\"headers\":{}}");
        int actionLen = source.getValue("$.action.length()");
        for (int i = 0; i < actionLen; i++) {
            String operation = source.getValue(String.format("$.action[%s].operation", i));
            String target = source.getValue(String.format("$.action[%s].target", i));
            String expression = source.getValue(String.format("$.action[%s].expression", i));

            switch (target) {
                case "headers":
                    handleHeaders(builder, operation, expression);
                    break;
                case "queryString":
                    //todo: unsupported now
                case "body":
                    //todo: unsupported now
                    break;
                default:
                    throw new ApiPlaneException("Unsupported target : " + target);
            }
        }
        return builder.jsonString();
    }

    private void handleHeaders(ResourceGenerator builder, String operation, String expression) {
        Matcher matcher = Pattern.compile("(.*?):(.*?)").matcher(expression);
        String leftValue = expression;
        String rightValue = expression;
        if (matcher.find()) {
            leftValue = matcher.group(1);
            rightValue = matcher.group(2);
        }
        switch (operation) {
            case "add": {
                String match = String.format("{\"regex\": \"%s\"}", getRegexByOp("=", rightValue));
                builder.createOrUpdateJson("$.headers", leftValue, match);
                break;
            }
            case "replace": {
                builder.createOrUpdateJson("$.headers", leftValue, rightValue);
                break;
            }
            case "rename":
                //todo: unsupported now
            case "remove":
                //todo: unsupported now
            case "append":
                //todo: unsupported now
                break;
            default:
                throw new ApiPlaneException("Unsupported operation : " + operation);
        }
    }
}