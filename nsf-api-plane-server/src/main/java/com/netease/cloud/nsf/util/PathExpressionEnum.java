package com.netease.cloud.nsf.util;

import com.netease.cloud.nsf.exception.ApiPlaneException;
import com.sun.javafx.binding.StringFormatter;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public enum PathExpressionEnum {
    YANXUAN_ADD_WHITE_LIST_PATH("$.spec.rules[?(@.services[?(@=='%s')])].constraints[*].values", 1),
    YANXUAN_REMOVE_WHITE_LIST_PATH("$.spec.rules[?(@.services[?(@=='%s')])].constraints[*].values[?(@=='%s')]", 2);

    private String expression;
    private int paramAmount;

    PathExpressionEnum(String expression) {
        this(expression, 0);
    }

    PathExpressionEnum(String expression, int paramAmount) {
        this.expression = expression;
        this.paramAmount = paramAmount;
    }

    public String translate(String... param) {
        if (param.length != this.paramAmount) {
            throw new ApiPlaneException(StringFormatter.format("Translate %d parameters are required", this.paramAmount).getValue());
        }
        return StringFormatter.format(expression, param).getValue();
    }
}
