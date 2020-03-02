package com.netease.cloud.nsf.web.controller;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.cache.ResourceCache;
import com.netease.cloud.nsf.cache.ResourceStoreFactory;
import com.netease.cloud.nsf.cache.meta.ServiceDto;
import com.netease.cloud.nsf.core.gateway.service.ConfigManager;
import com.netease.cloud.nsf.service.ServiceMeshService;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private ConfigManager configManager;


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
        workLoadByServiceInfo = resourceCache.getWorkLoadListWithSidecarVersion(workLoadByServiceInfo);
        checkResult(workLoadByServiceInfo);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", workLoadByServiceInfo);
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

    @RequestMapping(params = {"Action=GetAllWorkLoad"}, method = RequestMethod.GET)
    public String getAllWorkLoad(@RequestParam(name = "ClusterId", required = false) String clusterId,
                                 @RequestParam(name = "ProjectId",required = false) String projectId) {
        List workLoadList;
        if (StringUtils.isEmpty(clusterId)) {
            workLoadList = resourceCache.getAllWorkLoad(projectId);
        } else {
            if (!ResourceStoreFactory.listClusterId().contains(clusterId)) {
                throw new ApiPlaneException("ClusterId not found", 404);
            }
            workLoadList = resourceCache.getAllWorkLoadByClusterId(clusterId, projectId);
        }
        workLoadList = resourceCache.getWorkLoadListWithSidecarVersion(workLoadList);
        checkResult(workLoadList);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", workLoadList);
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

}
