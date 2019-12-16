package com.netease.cloud.nsf.web.controller;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.dto.PluginOrderDTO;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/26
 **/
@RestController
@RequestMapping(value = "/api", params = "Version=2019-07-25")
public class GatewayCommonController extends BaseController {

    @Autowired
    private GatewayService gatewayService;

    @RequestMapping(params = "Action=GetServiceList", method = RequestMethod.GET)
    public String getServiceList() {

        Map<String, Object> result = new HashMap<>();
        result.put(RESULT_LIST, gatewayService.getServiceList());
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    @RequestMapping(params = "Action=GetServiceAndPortList", method = RequestMethod.GET)
    public String getServiceAndPortList(@RequestParam(name = "Name", required = false) String name) {
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null,
                ImmutableMap.of("ServiceList", gatewayService.getServiceAndPortList(name)));
    }

    @RequestMapping(params = "Action=GetGatewayList", method = RequestMethod.GET)
    public String getGatewayList() {

        Map<String, Object> result = new HashMap<>();
        List<Gateway> gatewayList = gatewayService.getGatewayList();

        result.put(RESULT_LIST, gatewayList);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    @RequestMapping(params = "Action=GetPluginOrder", method = RequestMethod.GET)
    public String getPluginOrder() {
        Map<String, Object> result = new HashMap<>();

        result.put(RESULT, gatewayService.getPluginOrder());
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), null, result);
    }

    @RequestMapping(params = "Action=PublishPluginOrder", method = RequestMethod.POST)
    public String publishPluginOrder(@RequestBody @Valid PluginOrderDTO pluginOrderDTO) {

        gatewayService.updatePluginOrder(pluginOrderDTO);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(params = "Action=DeletePluginOrder", method = RequestMethod.POST)
    public String deletePluginOrder(@RequestBody @Valid PluginOrderDTO pluginOrderDTO) {

        gatewayService.deletePluginOrder(pluginOrderDTO);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

}
