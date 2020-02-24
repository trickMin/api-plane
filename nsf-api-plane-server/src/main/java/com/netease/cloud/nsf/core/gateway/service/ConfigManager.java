package com.netease.cloud.nsf.core.gateway.service;

import com.netease.cloud.nsf.meta.*;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.IstioResource;

import java.util.List;

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
     * 获取插件优先级
     * @param pluginOrder
     * @return
     */
    HasMetadata getConfig(PluginOrder pluginOrder);

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

    /**
     * 更新sidecar版本
     * @param svm
     */
    void updateConfig(SidecarVersionManagement svm);

    /**
     * 查询pod的sidecar版本
     * @param podVersion
     */
    List<PodStatus> querySVMConfig(PodVersion podVersion);

    /**
     * 更新全局插件
     * @param gp
     */
    void updateConfig(GlobalPlugin gp);

    /**
     * 删除全局插件
     * @param gp
s     */
    void deleteConfig(GlobalPlugin gp);

    /**
     * 查询网关
     * @param istioGateway
     * @return
     */
    HasMetadata getConfig(IstioGateway istioGateway);

    /**
     * 更新网关
     * @param istioGateway
     * @return
     */
    void updateConfig(IstioGateway istioGateway);

}
