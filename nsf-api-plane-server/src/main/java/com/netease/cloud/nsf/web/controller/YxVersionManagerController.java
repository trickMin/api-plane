package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author xy
 * 2019/11/13
 **/
@RestController
@RequestMapping(value = "/api", params = "Version=2019-11-13")
public class YxVersionManagerController extends BaseController {

    @Autowired
    private GatewayService gatewayService;

    @RequestMapping(value = "/svm", params = "Action=UpdateSVM", method = RequestMethod.POST)
    public String updateOrCreateSVM(@RequestBody @Valid SidecarVersionManagement svm) {

        gatewayService.updateSVM(svm);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/svm", params = "Action=QueryByPodNameList", method = RequestMethod.POST)
    public String queryByPodNameList(@RequestBody @Valid PodVersion podVersion) {

        List<PodStatus> list =  gatewayService.queryByPodNameList(podVersion);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", list);
        return apiReturn(SUCCESS, "Success", null, result);
    }

}
