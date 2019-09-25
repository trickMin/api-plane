package com.netease.cloud.nsf.core.gateway.service;


import me.snowdrop.istio.api.IstioResource;

import java.util.List;
import java.util.Map;

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

    List<IstioResource> get(String kind, String namespace);

    List<IstioResource> get(String kind, String namespace, Map<String, String> labels);

    boolean exist(IstioResource t);
}
