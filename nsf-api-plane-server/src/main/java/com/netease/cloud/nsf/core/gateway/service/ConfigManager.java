package com.netease.cloud.nsf.core.gateway.service;

import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.web.PortalService;
import me.snowdrop.istio.api.IstioResource;

import java.util.List;

/**
 *  API配置客户端
 */
public interface ConfigManager {

    /**
     * 更新API
     * @param api
     */
    void updateConfig(API api, String namespace);

    /**
     * 更新服务
     * @param service
     * @param namespace
     */
    void updateConfig(PortalService service, String namespace);

    /**
     * 删除API
     */
    void deleteConfig(API api, String namespace);

    /**
     * 删除服务
     * @param service
     * @param namespace
     */
    void deleteConfig(PortalService service, String namespace);

    /**
     * 获取服务对应的istio crd，一个服务对应一个istio crd，
     * 使用服务名称即可查询
     * @param service
     * @return
     */
    List<IstioResource> getConfigResources(String service, String namespace);

}
