package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.meta.dto.YxAPIDTO;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/19
 **/
@RestController
@RequestMapping(value = "/api", params = "Version=2019-07-25")
public class YxGatewayController extends BaseController {

    @Autowired
    private GatewayService gatewayService;

    @RequestMapping(value = "/yx", params = "Action=DeleteAPI", method = RequestMethod.POST)
    public String deleteApi(@RequestBody @Valid YxAPIDTO api) {
        gatewayService.deleteAPI(api);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/yx", params = "Action=PublishAPI", method = RequestMethod.POST)
    public String publishYXAPI(@RequestBody @Valid YxAPIDTO api) {
        gatewayService.updateAPI(api);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

}
