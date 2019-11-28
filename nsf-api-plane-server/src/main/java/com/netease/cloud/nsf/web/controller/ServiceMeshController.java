package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.service.ServiceMeshService;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * servicemesh产品化相关接口
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/7
 **/
@RestController
@RequestMapping(value = "/api/servicemesh", params = "Version=2019-07-25")
public class ServiceMeshController extends BaseController {

    @Autowired
    private ServiceMeshService istioService;

    @RequestMapping(params = "Action=UpdateConfig", method = RequestMethod.POST)
    public String updateConfig(@RequestBody String resource) {
        istioService.updateIstioResource(StringEscapeUtils.unescapeJava(resource));
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=DeleteConfig", method = RequestMethod.POST)
    public String deleteConfig(@RequestBody String resource) {
        istioService.deleteIstioResource(StringEscapeUtils.unescapeJava(resource));
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = {"Action=InjectSidecar"}, method = RequestMethod.GET)
    public String injectSidecar(@RequestParam(name = "Name") String name,
                                @RequestParam(name = "Namespace") String namespace,
                                @RequestParam(name = "ServiceVersion") String version,
                                @RequestParam(name = "Kind") String kind,
                                @RequestParam(name = "ClusterId") String clusterId) {

        ErrorCode code = istioService.sidecarInject(clusterId, kind, namespace, name, version);
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), null);
    }

}
