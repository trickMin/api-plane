package com.netease.cloud.nsf.core.editor;

import com.jayway.jsonpath.Predicate;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;

import java.util.Arrays;
import java.util.List;

/**
 * 提供给jsonpath的path expression
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public enum PathExpressionEnum {

    /** k8s资源公用 **/
    GET_KIND("$.kind", 0),
    GET_NAME("$.metadata.name", 0),
    GET_LABEL("$.metadata.labels", 0),
    GET_NAMESPACE("$.metadata.namespace", 0),
    GET_APIVERSION("$.apiVersion", 0),
    GET_RESOURCEVERSION("$.metadata.resourceVersion", 0),
    GET_ITEMS("$.items", 0),

    /** rbac **/
    REMOVE_RBAC_SERVICE("$.spec.rules[?]", 0),

    /** destinationrule **/
    REMOVE_DST_SUBSET_API("$.spec.subsets[?(@.api == '%s')]", 1),

    REMOVE_DST_SUBSET_NAME("$.spec.subsets[?(@.name == '%s')]", 1),

    /** sharedconfig **/
    REMOVE_SC_RATELIMITDESC("$.spec.rateLimitConfigs[*].descriptors[?(@.api == '%s')]", 1),

    REMOVE_SC_RATELIMITDESC_BY_CODE("$.spec.rateLimitConfigs[*].descriptors[?(@.code == '%s')]", 1),

    REMOVE_RATELIMIT_CONFIGMAP_BY_VALUE("$.descriptors[?(@.value =~ /.+Api\\[%s\\].+/i)]",1),
    /** plugin **/
    PLUGIN_GET_KIND("$.kind", 0),
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
            throw new ApiPlaneException(String.format("Translate %d parameters are required", this.paramAmount));
        }
        return String.format(expression, param);
    }

    public List<Predicate> filters() {
        return this.filters;
    }
}
