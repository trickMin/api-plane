package com.netease.cloud.nsf.util.function;

/**
 * 删除k8s资源片段
 * @param <T>
 */
@FunctionalInterface
public interface Subtracter<T> {

    T subtract(T t);
}
