package com.netease.cloud.nsf.web.controller;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.cache.ResourceCache;
import com.netease.cloud.nsf.cache.ResourceStoreFactory;
import com.netease.cloud.nsf.cache.meta.PodDTO;
import com.netease.cloud.nsf.cache.meta.ServiceDto;
import com.netease.cloud.nsf.cache.meta.WorkLoadDTO;
import com.netease.cloud.nsf.core.ConfigManager;
import com.netease.cloud.nsf.core.servicemesh.ServiceMeshConfigManager;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.service.ServiceMeshService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author zhangzihao
 */
@RestController
@RequestMapping(value = "/api/resource", params = "Version=2019-11-07")
public class K8sResourceController extends BaseController {

    @Autowired
    ResourceCache resourceCache;

    @Autowired
    private ServiceMeshService serviceMeshService;

    @Autowired
    private ServiceMeshConfigManager configManager;


    @RequestMapping(params = {"Action=GetWorkLoadByServiceInfo"}, method = RequestMethod.GET)
    public String getWorkLoadByServiceInfo(@RequestParam(name = "ServiceName") String serviceName,
                                           @RequestParam(name = "Namespace") String namespace,
                                           @RequestParam(name = "ProjectId") String projectId,
                                           @RequestParam(name = "ClusterId", required = false) String clusterId) {

        List workLoadByServiceInfo;
        if (!StringUtils.isEmpty(clusterId)) {
            if (!ResourceStoreFactory.listClusterId().contains(clusterId)) {
                throw new ApiPlaneException("ClusterId not found", 404);
            }
            workLoadByServiceInfo = resourceCache.getWorkLoadByServiceInfo(projectId, namespace, serviceName, clusterId);
        } else {
            workLoadByServiceInfo = resourceCache.getWorkLoadByServiceInfoAllClusterId(projectId, namespace, serviceName);
        }
        workLoadByServiceInfo.addAll(resourceCache.getServiceEntryWorkloadByServiceInfo(projectId,serviceName+ Const.SEPARATOR_DOT + namespace));
        checkResult(workLoadByServiceInfo);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", workLoadByServiceInfo);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=GetWorkLoadByLabel"}, method = RequestMethod.GET)
    public String getWorkLoadByLabel(@RequestParam(name = "Namespace") String namespace,
                                     @RequestParam(name = "ClusterId") String clusterId,
                                     @RequestParam(name = "ServiceLabel", required = false) List<String> labelList) {

        List<WorkLoadDTO> workLoadByLabels;
        if (StringUtils.isEmpty(clusterId)){
            workLoadByLabels = resourceCache.getWorkLoadByLabelsInAnyClusterId(labelList,namespace);
        }else {
            workLoadByLabels = resourceCache.getWorkLoadByLabels(clusterId,labelList,namespace);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("Result", workLoadByLabels);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }



    @RequestMapping(params = {"Action=GetPodByWorkLoad"}, method = RequestMethod.GET)
    public String getPodByWorkLoad(@RequestParam(name = "Name") String name,
                                   @RequestParam(name = "Namespace") String namespace,
                                   @RequestParam(name = "ClusterId") String clusterId,
                                   @RequestParam(name = "Kind") String kind) {

        if (!StringUtils.isEmpty(clusterId) && !ResourceStoreFactory.listClusterId().contains(clusterId)) {
            throw new ApiPlaneException("ClusterId not found", 404);
        }
        List podList = resourceCache.getPodDtoByWorkLoadInfo(clusterId, kind, namespace, name);
        checkResult(podList);
        String svmExpectedVersion = configManager.querySVMExpectedVersion(clusterId, namespace, kind, name);
        podList = resourceCache.getPodListWithSidecarVersion(podList, svmExpectedVersion);
        serviceMeshService.createMissingCrd(podList, kind, name, clusterId, namespace);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", podList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=GetPodBySidecarVersion"}, method = RequestMethod.GET)
    public String GetPodBySidecarVersion(@RequestParam(name = "SidecarVersion",required = false) String name,
                                   @RequestParam(name = "Namespace",required = false) String namespace,
                                   @RequestParam(name = "ClusterId",required = false) String clusterId) {


        List<PodDTO> podList = resourceCache.getPodList(clusterId, namespace);
        if (!StringUtils.isEmpty(name)){
            podList = podList.stream()
                    .filter(pod->pod.isInjected() && name.equals(pod.getSidecarVersion()))
                    .collect(Collectors.toList());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("Result", podList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=GetAllWorkLoad"}, method = RequestMethod.GET)
    public String getAllWorkLoad(@RequestParam(name = "ClusterId", required = false) String clusterId,
                                 @RequestParam(name = "ProjectId",required = false) String projectId) {
        List workLoadList;
        Map<String, Object> result = new HashMap<>();
        ErrorCode code = ApiPlaneErrorCode.Success;
        if (StringUtils.isEmpty(clusterId)) {
            workLoadList = resourceCache.getAllWorkLoad(projectId);
        } else {
            if (!ResourceStoreFactory.listClusterId().contains(clusterId)) {
                throw new ApiPlaneException("ClusterId not found", 404);
            }
            workLoadList = resourceCache.getAllWorkLoadByClusterId(clusterId, projectId);
        }
        workLoadList.addAll(resourceCache.getServiceEntryWorkLoad(projectId));
        checkResult(workLoadList);
        result.put("Result", workLoadList);
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=GetSidecarVersionOnWorkLoad"}, method = RequestMethod.GET)
    public String getSidecarVersionOnWorkLoad(@RequestParam(name = "Name") String name,
                                              @RequestParam(name = "Namespace") String namespace,
                                              @RequestParam(name = "ClusterId") String clusterId,
                                              @RequestParam(name = "Kind") String kind) {

        List<String> sidecarVersion = resourceCache.getSidecarVersionOnWorkLoad(clusterId,namespace,kind,name);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", sidecarVersion);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=GetNamespaceList"}, method = RequestMethod.GET)
    public String getNamespaceList(@RequestParam(name = "ClusterId", required = false) String clusterId) {

        List<String> namespaceList = ResourceStoreFactory.listNamespaceByClusterId(clusterId);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", namespaceList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=GetClusterIdList"}, method = RequestMethod.GET)
    public String getClusterIdList() {

        List<String> clusterIdList = ResourceStoreFactory.listClusterId();
        Map<String, Object> result = new HashMap<>();
        result.put("Result", clusterIdList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=GetServiceByProject"}, method = RequestMethod.GET)
    public String getServiceByProject(@RequestParam(name = "ClusterId", required = false) String clusterId,
                                      @RequestParam(name = "ProjectCode") String projectCode) {


        List<ServiceDto> serviceDtoList = resourceCache.getServiceByProjectCode(projectCode, clusterId);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", serviceDtoList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    private void checkResult(List result){
        if (CollectionUtils.isEmpty(result)){
            throw new ApiPlaneException("No resources found", 404);
        }
    }

    @RequestMapping(params = "Action=GetPathPatterns", method = RequestMethod.GET)
    public String getPathPatterns(@RequestParam(name = "ClusterId", required = false, defaultValue = "default") String clusterId,
                                  @RequestParam(name = "Namespace") String namespace,
                                  @RequestParam(name = "Name") String name) {
        List<String> pathPatterns = resourceCache.getMixerPathPatterns(clusterId, namespace, name);
        return apiReturn(ImmutableMap.of(RESULT, pathPatterns));
    }

    @RequestMapping(params = "Action=DeletePathPatterns", method = RequestMethod.GET)
    public String deletePathPatterns(@RequestParam(name = "ClusterId", required = false, defaultValue = "default") String clusterId,
                                     @RequestParam(name = "Namespace") String namespace,
                                     @RequestParam(name = "Name") String name) {
        resourceCache.deleteMixerPathPatterns(clusterId, namespace, name);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(params = "Action=UpdatePathPatterns", method = RequestMethod.POST)
    public String updatePathPatterns(@RequestParam(name = "ClusterId", required = false, defaultValue = "default") String clusterId,
                                     @RequestParam(name = "Namespace") String namespace,
                                     @RequestParam(name = "Name") String name,
                                     @RequestBody List<String> urlPatterns) {
        resourceCache.updateMixerPathPatterns(clusterId, namespace, name, urlPatterns);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(params = {"Action=GetWorkLoadByApp"}, method = RequestMethod.GET)
    public String getWorkLoadByApp(@RequestParam(name = "AppName") String appName,
                                           @RequestParam(name = "Namespace") String namespace,
                                           @RequestParam(name = "ClusterId", required = false) String clusterId) {

        List workLoadByServiceInfo;
        if (!StringUtils.isEmpty(clusterId)) {
            if (!ResourceStoreFactory.listClusterId().contains(clusterId)) {
                throw new ApiPlaneException("ClusterId not found", 404);
            }
            workLoadByServiceInfo = resourceCache.getWorkLoadByApp( namespace, appName, clusterId);
        } else {
            workLoadByServiceInfo = resourceCache.getWorkLoadByAppAllClusterId(namespace, appName);
        }
        workLoadByServiceInfo = resourceCache.getWorkLoadListWithSidecarVersion(workLoadByServiceInfo);
        checkResult(workLoadByServiceInfo);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", workLoadByServiceInfo);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=GetServiceEntryWorkLoad"}, method = RequestMethod.GET)
    public String GetServiceEntryWorkLoad(@RequestParam(name = "ProjectId") String projectCode) {

        List<WorkLoadDTO> vmEndPoints = resourceCache.getServiceEntryWorkLoad(projectCode);
        return apiReturn(ImmutableMap.of(RESULT_LIST,vmEndPoints));

    }

}
