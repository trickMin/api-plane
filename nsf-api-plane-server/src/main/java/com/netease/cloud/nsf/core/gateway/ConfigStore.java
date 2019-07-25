package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.meta.IstioResource;

/**
 * 配置持久化
 */
public interface ConfigStore {

    void create(IstioResource t);

    void delete(IstioResource t);

    void update(IstioResource t);

    /**
     * 根据kind,namespace,name来查询资源
     * @param t
     * @return
     */
    IstioResource get(IstioResource t);

    IstioResource get(String kind, String namespace, String name);

    boolean exist(IstioResource t);
}
