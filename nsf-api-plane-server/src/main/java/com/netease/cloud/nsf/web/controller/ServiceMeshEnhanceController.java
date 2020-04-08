package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.meta.dto.sm.ServiceMeshRateLimitDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 *
 * service mesh 增强功能
 **/
@RestController
@RequestMapping(value = "/api/servicemesh", params = "Version=2019-07-25")
public class ServiceMeshEnhanceController extends BaseController {

    @RequestMapping(params = "Action=UpdateRateLimit", method = RequestMethod.POST)
    public String updateRateLimit(@RequestBody ServiceMeshRateLimitDTO rateLimitDTO) {



        return apiReturn(SUCCESS, "Success", null, null);
    }
}
