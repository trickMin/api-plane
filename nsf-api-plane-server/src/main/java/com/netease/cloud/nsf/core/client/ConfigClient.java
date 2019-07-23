package com.netease.cloud.nsf.core.client;

/**
 *  配置发送客户端
 */
public interface ConfigClient {

    <T> void updateConfig(T t);

    void deleteConfig(String id);

    <T> T getConfig(String id);

}
