package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.ConfigManager;
import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;

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


    void updateRateLimit(ServiceMeshRateLimit rateLimit);
}
