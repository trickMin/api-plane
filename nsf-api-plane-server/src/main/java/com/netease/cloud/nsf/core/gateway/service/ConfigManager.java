package com.netease.cloud.nsf.core.gateway.service;

import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.PluginOrder;
import com.netease.cloud.nsf.meta.Service;

/**
 *  API配置客户端，用于发送配置
 */
public interface ConfigManager {

    /**
     * 更新API
     * @param api
     */
    void updateConfig(API api);

    /**
     * 更新服务
     * @param service
     */
    void updateConfig(Service service);

    /**
     * 删除API
     */
    void deleteConfig(API api);

    /**
     * 删除服务
     * @param service
     */
    void deleteConfig(Service service);

    /**
     * 更新插件优先级
     * @param pluginOrder
     */
    void updateConfig(PluginOrder pluginOrder);

    /**
     * 删除插件优先级
     * @param pluginOrder
     */
    void deleteConfig(PluginOrder pluginOrder);
}
