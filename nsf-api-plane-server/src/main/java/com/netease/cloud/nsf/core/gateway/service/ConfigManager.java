package com.netease.cloud.nsf.core.gateway.service;

import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.PluginOrder;
import com.netease.cloud.nsf.meta.Service;
import com.netease.cloud.nsf.meta.dto.PluginOrderDTO;

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
    void updateConfig(Service service, String namespace);

    /**
     * 删除API
     */
    void deleteConfig(API api, String namespace);

    /**
     * 删除服务
     * @param service
     * @param namespace
     */
    void deleteConfig(Service service, String namespace);

    void updateConfig(PluginOrder pluginOrder, String namespace);

    void deleteConfig(PluginOrder pluginOrder, String namespace);
}
