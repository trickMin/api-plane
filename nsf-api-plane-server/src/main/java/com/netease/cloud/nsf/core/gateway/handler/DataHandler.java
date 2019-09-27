package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;

import java.util.List;

/**
 * 处理数据
 * @param <T>
 */
public interface DataHandler<T> {

    List<TemplateParams> handle(T t);

}
