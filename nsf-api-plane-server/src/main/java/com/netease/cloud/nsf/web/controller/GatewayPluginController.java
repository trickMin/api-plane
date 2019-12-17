package com.netease.cloud.nsf.web.controller;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.meta.Plugin;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
@RestController
@RequestMapping(value = "/api/plugin", params = "Version=2019-07-25")
public class GatewayPluginController extends BaseController {

    @Autowired
    private PluginService pluginService;

    @RequestMapping(params = "Action=GetPluginDetail", method = RequestMethod.GET)
    public String getTemplate(@RequestParam("Name") String name) {

        Plugin plugin = pluginService.getPlugin(name);
        ErrorCode code = ApiPlaneErrorCode.Success;

        Map<String, Object> result = new HashMap<>();
        result.put("Schema", ResourceGenerator.newInstance(pluginService.getSchema(plugin.getSchema())).object(Object.class));
        result.put("Plugin", plugin);
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = "Action=GetPluginList", method = RequestMethod.GET)
    public String getPlugins() {
        Map<String, Plugin> plugins = pluginService.getPlugins();
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), ImmutableMap.of("Plugins", plugins.values()));
    }
}
