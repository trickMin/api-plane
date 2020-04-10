package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.ConfigManager;
import com.netease.cloud.nsf.meta.*;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/

public interface ServiceMeshConfigManager extends ConfigManager {

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
     * 查询负载的期望版本
     * @param workLoadType
     * @param workLoadName
     */
    String querySVMExpectedVersion(String clusterId, String namespace, String workLoadType, String workLoadName);

    IptablesConfig queryIptablesConfigByApp(String cluterId, String namespace, String appName);

    void updateRateLimit(ServiceMeshRateLimit rateLimit);

    void deleteRateLimit(ServiceMeshRateLimit rateLimit);
}
