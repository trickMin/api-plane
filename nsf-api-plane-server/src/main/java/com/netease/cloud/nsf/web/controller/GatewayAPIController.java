package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.YxAPIModel;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/19
 **/
@RestController
@RequestMapping(value = "/api", params = "Version=2019-07-25")
public class GatewayAPIController extends BaseController{

    @Autowired
    private GatewayService gatewayService;

    @RequestMapping(params = "Action=DeleteAPI", method = RequestMethod.GET)
    public String deleteApi() {
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(params = "Action=GetServiceList", method = RequestMethod.GET)
    public String getServiceList() {

        Map<String, Object> result = new HashMap<>();
        List<String> serviceList = gatewayService.getServiceList().stream().map(endpoint -> endpoint.getHostname()).distinct().collect(Collectors.toList());

        result.put(RESULT_LIST, serviceList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    @RequestMapping(params = "Action=GetGatewayList", method = RequestMethod.GET)
    public String getGatewayList() {

        Map<String, Object> result = new HashMap<>();
        List<Gateway> gatewayList = gatewayService.getGatewayList();

        result.put(RESULT_LIST, gatewayList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }


    @RequestMapping(value = "/yx", params = "Action=PublishAPI", method = RequestMethod.POST)
    public String publishAPI(@RequestBody @Valid YxAPIModel api) {
        gatewayService.updateAPI(api);
        return apiReturn(ApiPlaneErrorCode.Success);
    }
}