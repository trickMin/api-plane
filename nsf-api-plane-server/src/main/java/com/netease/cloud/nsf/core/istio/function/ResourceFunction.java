package com.netease.cloud.nsf.core.istio.function;

public interface ResourceFunction<T> {

    T apply(T t);

    boolean match(Object o);

}
