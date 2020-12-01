package com.netease.cloud.nsf.cache;

import com.netease.cloud.nsf.cache.meta.PodDTO;
import com.netease.cloud.nsf.cache.meta.ServiceDto;
import com.netease.cloud.nsf.cache.meta.WorkLoadDTO;
import io.fabric8.kubernetes.api.model.Namespace;
import me.snowdrop.istio.api.networking.v1alpha3.VersionManager;

import java.util.List;
import java.util.Map;

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


    List<WorkLoadDTO> getServiceEntryWorkLoad(String projectCode);

    List<WorkLoadDTO> getServiceEntryWorkloadByServiceInfo(String projectCode, String serviceName);

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

    List getPodListWithSidecarVersion(List podDTOList, String expectedVersion);

    List getPodListByService(String clusterId, String namespace, String name);

    List<VersionManager> getVersionManagerByClusterId(String clusterId);

    List<String> getMixerPathPatterns(String clusterId, String namespace, String name);

    void deleteMixerPathPatterns(String clusterId, String namespace, String name);

    void updateMixerPathPatterns(String clusterId, String namespace, String name, List<String> urlPatterns);

    String getAppNameByPod(String clusterId, String namespace, String name);

    String getPodLabel(String clusterId, String namespace, String name, String labelName);

    List<PodDTO> getPodListByClusterIdAndNamespace(String clusterId, String namespace);


    List<PodDTO> getPodList(String clusterId, String namespace);

    List getEndPointByService(String clusterId, String namespace, String name);

    List<ServiceDto> getServiceByProjectCode(String projectCode, String clusterId);

    List<WorkLoadDTO> getWorkLoadByApp(String namespace, String appName, String clusterId);

    List<WorkLoadDTO> getWorkLoadByAppAllClusterId(String namespace, String appName);

    List<WorkLoadDTO> getWorkLoadByLabelsInAnyClusterId(List<String> labelsList, String namespace);

    List<WorkLoadDTO> getWorkLoadByLabels(String clusterId, List<String> labelsList, String namespace);

    List<String> getSidecarVersionOnWorkLoad(String clusterId, String namespace, String kind, String name);

    <T> List<T> getServiceByClusterAndNamespace(String clusterId,String namespace);

    void updateNamespaceLabel(String clusterId, String namespace, Map<String, String> labels);

    List<Namespace> getNamespaces(String clusterId);

    Map<String, List<Namespace>> getNamespaces();

    Map<String, Map<String, String>> getSyncz(String type, String version);
}
