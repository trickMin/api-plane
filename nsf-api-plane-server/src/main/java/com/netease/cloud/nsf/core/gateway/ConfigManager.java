package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.YxAPIModel;
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
    void updateConfig(API api);

    /**
     * 删除API
     * @param service
     * @param name
     */
    void deleteConfig(String service, String name);

    /**
     * 获取服务对应的istio crd，一个服务对应一个istio crd，
     * 使用服务名称即可查询
     * @param service
     * @return
     */
    List<IstioResource> getConfigResources(String service);

}
