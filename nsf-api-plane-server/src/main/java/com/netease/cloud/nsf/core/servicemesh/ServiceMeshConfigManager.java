package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.ConfigManager;
import com.netease.cloud.nsf.meta.*;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/

public interface ServiceMeshConfigManager extends ConfigManager {

    /**
     * 更新cr
     * @param resources
     * @param clusterId
     */
    void updateConfig(List<HasMetadata> resources, String clusterId);

    /**
     * 更新sidecar版本
     * @param svm
     */
    void updateConfig(SidecarVersionManagement svm, String clusterId);

    /**
     * 查询pod的sidecar版本
     * @param podVersion
     */
    List<PodStatus> querySVMConfig(PodVersion podVersion, String clusterId);

    /**
     * 查询负载的期望版本
     * @param workLoadType
     * @param workLoadName
     */
    String querySVMExpectedVersion(String clusterId, String namespace, String workLoadType, String workLoadName);

    IptablesConfig queryIptablesConfigByApp(String clusterId, String namespace, String appName);

    void updateRateLimit(ServiceMeshRateLimit rateLimit);

    void updateCircuitBreaker(ServiceMeshCircuitBreaker circuitBreaker);

    void deleteRateLimit(ServiceMeshRateLimit rateLimit);

    /**
     *
     * @param sourceService  源服务名 , app
     * @param sourceNamespace 源服务namespace
     * @param targetService 目标服务，全名 - a.default.svc.cluster.local
     */
    void updateSidecarScope(String sourceService, String sourceNamespace, String targetService);
}
