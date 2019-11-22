package com.netease.cloud.nsf.cache;

/**
 * @author zhangzihao
 */
public interface Informer {


    /**
     * informer开始监听资源更新事件
     */
    void start();

    /**
     * 同步资源信息
     */
    void replaceResource();
}
