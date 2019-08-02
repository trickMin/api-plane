package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.meta.PluginTemplate;
import com.netease.cloud.nsf.service.PluginService;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/2
 **/
@RestController
@RequestMapping(value = "/api/plugin", params = "Version=2018-05-31")
public class GatewayPluginController extends BaseController {

    @Autowired
    private PluginService pluginService;

    @RequestMapping(params = "Action=GetTemplate", method = RequestMethod.GET)
    public String getTemplate(@RequestParam("name") String name, @RequestParam("version") String version) {

        PluginTemplate template = pluginService.getTemplate(name, version);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(),code.getMessage(), new HashMap(){{put("Result", template);}});
    }

    @Autowired
    private EditorContext editorContext;

    @RequestMapping(params = "Action=ProcessTemplate", method = RequestMethod.POST)
    public String processTemplate(@RequestParam("name") String name, @RequestParam("version") String version, @RequestBody String plugin){
        String result = pluginService.processTemplate(name, version, plugin, ResourceType.JSON);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), new HashMap(){{put("Result", ResourceGenerator.newInstance(result, ResourceType.JSON, editorContext).object(Map.class));}});
    }

}
