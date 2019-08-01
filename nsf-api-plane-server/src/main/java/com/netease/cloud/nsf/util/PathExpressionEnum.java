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
    YANXUAN_GET_KIND("$.kind", 0),
    YANXUAN_GET_NAME("$.metadata.name", 0),
    YANXUAN_GET_NAMESPACE("$.metadata.namespace", 0),
    YANXUAN_GET_APIVERSION("$.apiVersion", 0),
    YANXUAN_GET_RESOURCEVERSION("$.metadata.resourceVersion", 0),
    YANXUAN_GET_ITEMS("$.items", 0),
    YANXUAN_REMOVE_RBAC_SERVICE("$.spec.rules[?]", 0),
    YANXUAN_ADD_RBAC_SERVICE("$.spec.rules", 0),
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
