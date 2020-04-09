package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.cache.K8sResourceCache;
import com.netease.cloud.nsf.cache.ResourceStoreFactory;
import com.netease.cloud.nsf.cache.meta.PodDTO;
import com.netease.cloud.nsf.configuration.MeshConfig;
import com.netease.cloud.nsf.meta.IptablesConfig;
import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.meta.SVMSpec;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.service.SVMIptablesHelper;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author xy
 * 2019/11/13
 **/
@RestController
@RequestMapping(value = "/api", params = "Version=2019-11-13")
public class YxVersionManagerController extends BaseController {

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private K8sResourceCache k8sResourceCache;

    @Autowired
    private MeshConfig meshConfig;

    @RequestMapping(value = "/svm", params = "Action=UpdateSVM", method = RequestMethod.POST)
    public String updateOrCreateSVM(@RequestBody @Valid SidecarVersionManagement svm) {

        if (!ResourceStoreFactory.listClusterId().contains(svm.getClusterId())) {
            return apiReturn(ApiPlaneErrorCode.CanNotFound("ClusterId"));
        }

        for (SVMSpec svmSpec : svm.getWorkLoads()) {
            if (!(svmSpec.getWorkLoadType().equals("Deployment") || svmSpec.getWorkLoadType().equals("StatefulSet"))) {
                return apiReturn(ApiPlaneErrorCode.ParameterError("WorkLoadType"));
            }

            Object obj = k8sResourceCache.getResource(svm.getClusterId(), svmSpec.getWorkLoadType(), svm.getNamespace(), svmSpec.getWorkLoadName());
            if (obj == null) {
                return apiReturn(ApiPlaneErrorCode.workLoadNotFound);
            }

            List<PodDTO> podDTOList =  k8sResourceCache.getPodDtoByWorkLoadInfo(svm.getClusterId(), svmSpec.getWorkLoadType(), svm.getNamespace(), svmSpec.getWorkLoadName());
            if (CollectionUtils.isEmpty(podDTOList)) {
                return apiReturn(ApiPlaneErrorCode.workLoadNotInMesh);
            }
            if(!isInMesh(podDTOList)) {
                return apiReturn(ApiPlaneErrorCode.workLoadNotInMesh);
            }

            svmSpec.setIptablesDetail(null);
            svmSpec.setIptablesParams(null);

        }

        gatewayService.updateSVM(svm);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/svm", params = "Action=UpdateIptables", method = RequestMethod.POST)
    public String updateOrCreateSVMForIptables(@RequestBody IptablesConfig iptablesConfig,
                                               @RequestParam(name = "ClusterId") String clusterId,
                                               @RequestParam(name = "Namespace") String namespace,
                                               @RequestParam(name = "AppName") String appName) {

        if (!ResourceStoreFactory.listClusterId().contains(clusterId)) {
            return apiReturn(ApiPlaneErrorCode.CanNotFound("ClusterId"));
        }

        SVMSpec svmSpec = new SVMSpec();
        svmSpec.setWorkLoadType("LabelSelector");
        svmSpec.setLabels(new HashMap<>());
        svmSpec.getLabels().put(meshConfig.getSelectorAppKey(), appName);
        svmSpec.setIptablesParams(SVMIptablesHelper.processIptablesConfigAndBuildParams(iptablesConfig));
        svmSpec.setIptablesDetail(iptablesConfig.toString());
		SidecarVersionManagement svm = new SidecarVersionManagement();
		svm.setClusterId(clusterId);
		svm.setNamespace(namespace);
		svm.setWorkLoads(Arrays.asList(svmSpec));
        gatewayService.updateSVM(svm);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/svm", params = "Action=GetIptables", method = RequestMethod.GET)
    public IptablesConfig updateOrCreateSVMForIptables(@RequestParam(name = "ClusterId") String clusterId,
                                                       @RequestParam(name = "Namespace") String namespace,
                                                       @RequestParam(name = "AppName") String appName) {
        return gatewayService.queryIptablesConfigByApp(clusterId, namespace, appName);
    }

    @RequestMapping(value = "/svm", params = "Action=QueryByPodNameList", method = RequestMethod.POST)
    public String queryByPodNameList(@RequestBody @Valid PodVersion podVersion) {

        if (!ResourceStoreFactory.listClusterId().contains(podVersion.getClusterId())) {
            return apiReturn(ApiPlaneErrorCode.CanNotFound("ClusterId"));
        }

        List<PodStatus> list =  gatewayService.queryByPodNameList(podVersion);
        if (list == null) {
            return apiReturn(ApiPlaneErrorCode.resourceNotFound);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("Result", list);
        return apiReturn(SUCCESS, "Success", null, result);
    }


    private boolean isInMesh(List<PodDTO> podDTOList) {
        PodVersion podVersion = new PodVersion();
        List<String> nameList =
                podDTOList.stream()
                        .map(o -> o.getName())
                        .collect(Collectors.toList());
        podVersion.setPodNames(nameList);
        podVersion.setClusterId(podDTOList.get(0).getClusterId());
        podVersion.setNamespace(podDTOList.get(0).getNamespace());

        List<PodStatus> list =  gatewayService.queryByPodNameList(podVersion);
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }
        return true;
    }

}
