package com.netease.cloud.nsf.web.controller;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.meta.PluginTemplate;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
@RestController
@RequestMapping(value = "/api/plugin", params = "Version=2019-07-25")
public class GatewayPluginController extends BaseController {

    @Autowired
    private PluginService pluginService;

    @RequestMapping(params = "Action=GetTemplate", method = RequestMethod.GET)
    public String getTemplate(@RequestParam("Name") String name, @RequestParam("Version") String version) {

        PluginTemplate template = pluginService.getTemplate(name, version);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), ImmutableMap.of("Result", template));
    }
}
