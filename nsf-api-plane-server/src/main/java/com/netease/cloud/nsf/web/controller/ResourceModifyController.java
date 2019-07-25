package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import me.snowdrop.istio.api.rbac.v1alpha1.ServiceRole;
import me.snowdrop.istio.mixer.adapter.rbac.Rbac;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.netease.cloud.nsf.util.PathExpressionEnum.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/24
 **/
@RestController
@RequestMapping(value = "/api/istio/modify", params = "Version=2018-05-31")
public class ResourceModifyController extends BaseController {

    @Autowired
    private EditorContext editorContext;

    @RequestMapping(params = "Action=UpdateValue", method = RequestMethod.POST)
    public String updateListNodeByKV(@RequestBody String json) {
        ResourceGenerator generator = ResourceGenerator.newInstance(json, ResourceType.JSON, editorContext);
        generator.addElement(YANXUAN_ADD_WHITE_LIST_PATH.translate("yx-provider.yx-demo.svc.cluster.local"), "yx-consumer2");
        return apiReturn(200, generator.jsonString());
    }

    @RequestMapping(params = "Action=DeleteValue", method = RequestMethod.POST)
    public String deleteListNodeByKV(@RequestBody String json) {
        ResourceGenerator generator = ResourceGenerator.newInstance(json, ResourceType.JSON, editorContext);
        generator.removeElement(YANXUAN_REMOVE_WHITE_LIST_PATH.translate("yx-provider.yx-demo.svc.cluster.local", "yx-consumer"));
        return apiReturn(200, generator.jsonString());
    }

    @RequestMapping(params = "Action=GetValue", method = RequestMethod.POST)
    public String getListNodeByKV(@RequestBody String json) {
        ResourceGenerator generator = ResourceGenerator.newInstance(json, ResourceType.JSON, editorContext);
        JSONArray value = generator.getValue("$.spec.rules[?(@.services[?(@=='yx-provider.yx-demo.svc.cluster.local')])].constraints[*].values[?(@=='yx-consumer')]", JSONArray.class);
        return apiReturn(200, generator.jsonString());
    }

    @RequestMapping(params = "Action=GetObject", method = RequestMethod.POST)
    public String getObject(@RequestBody String json) {
        ResourceGenerator generator = ResourceGenerator.newInstance(json, ResourceType.JSON, editorContext);
        Object object = generator.object(ServiceRole.class);
        return null;
    }

}
