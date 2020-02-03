package com.netease.cloud.nsf.web.controller;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.meta.Graph;
import com.netease.cloud.nsf.service.ServiceMeshService;
import com.netease.cloud.nsf.service.TopoService;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

/**
 * servicemesh产品化相关接口
 *
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/7
 **/
@RestController
@RequestMapping(value = "/api/servicemesh", params = "Version=2019-07-25")
public class ServiceMeshController extends BaseController {

    @Autowired
    private ServiceMeshService istioService;

    @Autowired
    private TopoService topoService;

    private static final String DURATION_PATTERN = "\\d+(s|m|h|d)";

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

    @RequestMapping(params = "Action=GetConfig", method = RequestMethod.GET)
    public String deleteConfig(@RequestParam(name = "Name")String name,
                               @RequestParam(name = "Namespace")String namespace,
                               @RequestParam(name = "Kind")String kind) {
        return apiReturn(ImmutableMap.of(RESULT, istioService.getIstioResource(name, namespace, kind)));
    }

    @RequestMapping(params = {"Action=InjectSidecar"}, method = RequestMethod.GET)
    public String injectSidecar(@RequestParam(name = "Name") String name,
                                @RequestParam(name = "Namespace") String namespace,
                                @RequestParam(name = "ServiceVersion") String version,
                                @RequestParam(name = "SidecarVersion") String sidecarVersion,
                                @RequestParam(name = "Kind") String kind,
                                @RequestParam(name = "ClusterId") String clusterId) {

        ErrorCode code = istioService.sidecarInject(clusterId, kind, namespace, name, version, sidecarVersion);
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), null);
    }

    @RequestMapping(params = "Action=GetTopo", method = RequestMethod.GET)
    public String getTopo(@RequestParam(name = "Namespaces") String namespaces,
                          @RequestParam(name = "GraphType") String graphType,
                          @RequestParam(name = "Duration") String duration) {

        if (StringUtils.isEmpty(namespaces)) {
            return apiReturn(ApiPlaneErrorCode.InvalidFormat("duration"));
        }

        if (!"app".equals(graphType) && !"versionedApp".equals(graphType)) {
            return apiReturn(ApiPlaneErrorCode.InvalidFormat("graphType"));
        }

        if (StringUtils.isEmpty(duration) || !Pattern.matches(DURATION_PATTERN, duration)) {
            return apiReturn(ApiPlaneErrorCode.InvalidFormat("duration"));
        }

        Graph graph = topoService.getAppGraph(namespaces, duration, graphType);
        return apiReturn(ImmutableMap.of(RESULT, graph.getElements()));
    }

    @RequestMapping(params = "Action=NotifySidecarEvent", method = RequestMethod.GET)
    public String notifySidecarDownload(@RequestParam(name = "SidecarVersion") String version,
                                        @RequestParam(name = "Type") String type){
        istioService.notifySidecarFileEvent(version, type);
        return apiReturn(SUCCESS, "Success", null, null);
    }


}
