package com.netease.cloud.nsf.util;

import com.jayway.jsonpath.Predicate;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.sun.javafx.binding.StringFormatter;

import java.util.Arrays;
import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public enum PathExpressionEnum {
    GET_KIND("$.kind", 0),
    GET_NAME("$.metadata.name", 0),
    GET_NAMESPACE("$.metadata.namespace", 0),
    GET_APIVERSION("$.apiVersion", 0),
    GET_RESOURCEVERSION("$.metadata.resourceVersion", 0),
    GET_ITEMS("$.items", 0),
    REMOVE_RBAC_SERVICE("$.spec.rules[?]", 0),
    ADD_RBAC_SERVICE("$.spec.rules", 0),
    ADD_RBAC_SERVICE_TO_RBAC_CONFIG("$.spec.inclusion.services", 0),
    REMOVE_VS_HTTP("$.spec.http[?(@.name == '%s')]", 1),
    REMOVE_DST_SUBSET("$.spec.subsets[?(@.api == '%s')]", 1),

    PLUGIN_GET_KIND("$.kind", 0),
    PLUGIN_GET_VERSION("$.version", 0),

    ISTIO_GET_SVC("$[*].ep[*]", 0),
    ISTIO_GET_GATEWAY("$[?(@.svc =~ /%s/i)].ep[*]",1),
    ;

    private String expression;
    private int paramAmount;
    private List<Predicate> filters;


    PathExpressionEnum(String expression, int paramAmount, Predicate... filter) {
        this.expression = expression;
        this.paramAmount = paramAmount;
        if (filter != null && filter.length != 0) {
            this.filters = Arrays.asList(filter);
        }
    }

    public String translate(String... param) {
        if (param.length != this.paramAmount) {
            throw new ApiPlaneException(StringFormatter.format("Translate %d parameters are required", this.paramAmount).getValue());
        }
        return StringFormatter.format(expression, param).getValue();
    }

    public List<Predicate> filters() {
        return this.filters;
    }
}
