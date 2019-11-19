package com.netease.cloud.nsf.cache;


@FunctionalInterface
public interface ResourceUpdatedListener<T extends ResourceUpdateEvent> {

    void notify(T e);


}