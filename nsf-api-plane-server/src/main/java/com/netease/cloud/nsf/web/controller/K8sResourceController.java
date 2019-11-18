package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.cache.ResourceCache;
import com.netease.cloud.nsf.cache.ResourceStoreFactory;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
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
            workLoadByServiceInfo = resourceCache.getWorkLoadByServiceInfo(projectId, namespace, serviceName, clusterId);
        } else {
            workLoadByServiceInfo = resourceCache.getWorkLoadByServiceInfoAllClusterId(projectId, namespace, serviceName);
        }
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

        List podList = resourceCache.getPodByWorkLoadInfo(clusterId, kind, namespace, name);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", podList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=GetAllWorkLoad"}, method = RequestMethod.GET)
    public String getAllWorkLoad(@RequestParam(name = "ClusterId", required = false) String clusterId) {
        List podList;
        if (StringUtils.isEmpty(clusterId)) {
            podList = resourceCache.getAllWorkLoad();
        } else {
            podList = resourceCache.getAllWorkLoadByClusterId(clusterId);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("Result", podList);
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


}
