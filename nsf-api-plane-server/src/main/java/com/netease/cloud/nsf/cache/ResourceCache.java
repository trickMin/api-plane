package com.netease.cloud.nsf.cache;

import java.util.List;

/**
 * @author zhangzihao
 */
public interface ResourceCache {


    /**
     * 获取与指定Service 关联的工作负载资源（Deploy Sts）列表
     *
     * @param projectId 项目标识
     * @param namespace 命名空间
     * @param serviceName Service名称
     * @param clusterId 集群Id
     * @return 负载列表
     */
    List getWorkLoadByServiceInfo(String projectId, String namespace, String serviceName, String clusterId);

    /**
     * 或取与指定负载信息关联的Pod列表
     * @param clusterId 集群标识
     * @param kind 资源类型
     * @param namespace 命名空间
     * @param name 资源名称
     * @return Pod 列表
     */
    List getPodDtoByWorkLoadInfo(String clusterId, String kind, String namespace, String name);


    List getPodInfoByWorkLoadInfo(String clusterId, String kind, String namespace, String name);

    /**
     * @return 返回网格中全部的负载资源
     * @param projectId
     */
    List getAllWorkLoad(String projectId);

    /**
     * @return 返回网格中指定集群负载资源
     */
    List getAllWorkLoadByClusterId(String clusterId, String projectId);


    List getWorkLoadByServiceInfoAllClusterId(String projectId, String namespace, String serviceName);

    /**
     *
     * @param clusterId
     * @param kind 资源类型
     * @param namespace 名称空间
     * @param name 资源名称
     * @return 资源列表
     */
    Object getResource(String clusterId, String kind, String namespace, String name);


    List getWorkLoadListWithSidecarVersion(List workLoadDTOList);

    List getPodListWithSidecarVersion(List podDTOList);

    List getPodListByService(String clusterId, String namespace, String name);

    List<String> getUrlMetricPatterns(String clusterId, String namespace, String app);

    String getAppNameByPod(String clusterId, String namespace, String name);

    List getEndPointByService(String clusterId, String namespace, String name);
}
