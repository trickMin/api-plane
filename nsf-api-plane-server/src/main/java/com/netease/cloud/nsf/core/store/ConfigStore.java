package com.netease.cloud.nsf.core.store;

/**
 * 配置持久化
 */
public interface ConfigStore {

    <T> void create(T t);

    <T> void delete(T t);

    <T> void update(T t);

    <T> T get(T t);
}
