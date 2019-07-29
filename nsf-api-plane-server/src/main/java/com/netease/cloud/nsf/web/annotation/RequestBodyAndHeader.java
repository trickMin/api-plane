package com.netease.cloud.nsf.web.annotation;


import java.lang.annotation.*;

/**
 * 标记Request Parameter
 * 会同时提取request Body 和 header的参数并注入到bean中
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/29
 **/
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBodyAndHeader {
    boolean required() default true;
}
