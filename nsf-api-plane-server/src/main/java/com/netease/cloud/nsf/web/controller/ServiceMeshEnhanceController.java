package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.core.servicemesh.ServiceMeshConfigManager;
import com.netease.cloud.nsf.meta.dto.ServiceMeshCircuitBreakerDTO;
import com.netease.cloud.nsf.meta.dto.sm.ServiceMeshRateLimitDTO;
import com.netease.cloud.nsf.service.ServiceMeshEnhanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 *
 * service mesh 增强功能
 **/
@RestController
@RequestMapping(value = "/api/servicemesh", params = "Version=2019-07-25")
public class ServiceMeshEnhanceController extends BaseController {

    @Autowired
    private ServiceMeshEnhanceService serviceMeshEnhanceService;

    @Autowired
    private ServiceMeshConfigManager configManager;

    @RequestMapping(params = "Action=UpdateRateLimit", method = RequestMethod.POST)
    public String updateRateLimit(@RequestBody ServiceMeshRateLimitDTO rateLimitDTO) {

        serviceMeshEnhanceService.updateRateLimit(rateLimitDTO);
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=DeleteRateLimit", method = RequestMethod.POST)
    public String deleteRateLimit(@RequestBody ServiceMeshRateLimitDTO rateLimitDTO) {

        serviceMeshEnhanceService.deleteRateLimit(rateLimitDTO);
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=UpdateCircuitBreaker", method = RequestMethod.POST)
    public String updateCircuitBreaker(@RequestBody ServiceMeshCircuitBreakerDTO circuitBreakerDTO) {

        serviceMeshEnhanceService.updateServiceMeshCircuitBreaker(circuitBreakerDTO);


        return apiReturn(SUCCESS, "Success", null, null);
    }

    // for test only
    @RequestMapping(params = "Action=UpdateSidecarScope", method = RequestMethod.GET)
    public String updateRateLimit(@RequestParam(value = "App") String app,
                                  @RequestParam(value = "Ns") String ns,
                                  @RequestParam(value = "Target") String target) {

        configManager.updateSidecarScope(app, ns, target);
        return apiReturn(SUCCESS, "Success", null, null);
    }

}
