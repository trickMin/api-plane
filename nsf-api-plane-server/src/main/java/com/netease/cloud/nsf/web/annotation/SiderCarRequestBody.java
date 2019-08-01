package com.netease.cloud.nsf.web.annotation;

import java.lang.annotation.*;

/**
 * 解析从SiderCar发出的请求Header
 * 并自动注入到com.netease.cloud.nsf.meta.SiderCarRequestMeta类中
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/29
 **/
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SiderCarRequestBody {
    /**
     * metaName为引入SiderCarRequestMeta类的propertyName
     * @return
     */
    String metaName() default "siderCarMeta";
}
