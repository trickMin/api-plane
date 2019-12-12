package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.cache.ResourceCache;
import com.netease.cloud.nsf.cache.ResourceStoreFactory;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
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
        List podList = resourceCache.getPodByWorkLoadInfo(clusterId, kind, namespace, name);
        checkResult(podList);
        podList = resourceCache.getPodListWithSidecarVersion(podList);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", podList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=GetAllWorkLoad"}, method = RequestMethod.GET)
    public String getAllWorkLoad(@RequestParam(name = "ClusterId", required = false) String clusterId) {
        List workLoadList;
        if (StringUtils.isEmpty(clusterId)) {
            workLoadList = resourceCache.getAllWorkLoad();
        } else {
            if (!ResourceStoreFactory.listClusterId().contains(clusterId)) {
                throw new ApiPlaneException("ClusterId not found", 404);
            }
            workLoadList = resourceCache.getAllWorkLoadByClusterId(clusterId);
        }
        workLoadList = resourceCache.getWorkLoadListWithSidecarVersion(workLoadList);
        checkResult(workLoadList);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", workLoadList);
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

    private void checkResult(List result){
        if (CollectionUtils.isEmpty(result)){
            throw new ApiPlaneException("No resources found", 404);
        }
    }




}
