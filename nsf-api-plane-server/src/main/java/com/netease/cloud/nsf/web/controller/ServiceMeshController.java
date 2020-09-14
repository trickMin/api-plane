package com.netease.cloud.nsf.web.controller;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.meta.Graph;
import com.netease.cloud.nsf.meta.ValidateResult;
import com.netease.cloud.nsf.meta.dto.ValidateResultDTO;
import com.netease.cloud.nsf.service.ServiceMeshService;
import com.netease.cloud.nsf.service.TopoService;
import com.netease.cloud.nsf.service.ValidateService;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.Trans;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * servicemesh产品化相关接口
 *
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/7
 **/
@RestController
@RequestMapping(value = "/api/servicemesh", params = "Version=2019-07-25")
@Validated
public class ServiceMeshController extends BaseController {

    @Autowired
    private ServiceMeshService serviceMeshService;

    @Autowired
    private TopoService topoService;

    @Autowired
    private ValidateService validateService;

    private static final String DURATION_PATTERN = "\\d+(s|m|h|d)";

    @RequestMapping(params = "Action=UpdateConfig", method = RequestMethod.POST)
    public String updateConfig(@RequestBody String resource,
                               @RequestParam(name = "ClusterId", required = false) String clusterId) {
        serviceMeshService.updateIstioResource(StringEscapeUtils.unescapeJava(resource), clusterId);
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=DeleteConfig", method = RequestMethod.POST)
    public String deleteConfig(@RequestBody String resource,
                               @RequestParam(name = "ClusterId", required = false) String clusterId) {
        serviceMeshService.deleteIstioResource(StringEscapeUtils.unescapeJava(resource), clusterId);
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=GetConfig", method = RequestMethod.GET)
    public String getConfig(@RequestParam(name = "Name")String name,
                            @RequestParam(name = "Namespace")String namespace,
                            @RequestParam(name = "Kind")String kind,
                            @RequestParam(name = "ClusterId", required = false) String clusterId) {
        return apiReturn(ImmutableMap.of(RESULT, serviceMeshService.getIstioResource(clusterId, name, namespace, kind)));
    }

    @RequestMapping(params = "Action=GetConfigsByNamespaces", method = RequestMethod.GET)
    public String getConfigsByNamespaces(@RequestParam(name = "Namespaces") String namespaces,
                                         @RequestParam(name = "Kind") String kind,
                                         @RequestParam(name = "ClusterId", required = false) String clusterId) {
        return apiReturn(ImmutableMap.of(RESULT_LIST, serviceMeshService.getIstioResourceList(clusterId, namespaces, kind)));
    }

    @RequestMapping(params = {"Action=InjectSidecar"}, method = RequestMethod.GET)
    public String injectSidecar(@RequestParam(name = "Name") String name,
                                @RequestParam(name = "Namespace") String namespace,
                                @RequestParam(name = "ServiceVersion") String version,
                                @RequestParam(name = "SidecarVersion") String sidecarVersion,
                                @RequestParam(name = "Kind") String kind,
                                @RequestParam(name = "ClusterId") String clusterId,
                                @RequestParam(name = "AppName",required = false) String appName) {

        ErrorCode code = serviceMeshService.sidecarInject(clusterId, kind, namespace, name, version, sidecarVersion, appName);
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), null);
    }

    @RequestMapping(params = {"Action=CreateAppOnService"}, method = RequestMethod.GET)
    public String createAppOnService(@RequestParam(name = "Name") String name,
                                @RequestParam(name = "Namespace") String namespace,
                                @RequestParam(name = "ClusterId",required = false) String clusterId,
                                @RequestParam(name = "AppName") String appName) {

        ErrorCode code = serviceMeshService.createAppOnService(clusterId, namespace, name, appName);
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), null);
    }

    @RequestMapping(params = "Action=GetTopo", method = RequestMethod.GET)
    public String getTopo(@RequestParam(name = "Namespaces") String namespaces,
                          @RequestParam(name = "GraphType") String graphType,
                          @RequestParam(name = "Duration") String duration,
                          @RequestParam(name = "InjectServices", defaultValue = "false") Boolean injectServices,
                          @RequestParam(name = "FocalizeApps", required = false) List<String> focalizeApps,
                          @RequestParam(name = "FocalizeSize", required = false, defaultValue = "0") int focalizeSize) {

        if (StringUtils.isEmpty(namespaces)) {
            return apiReturn(ApiPlaneErrorCode.InvalidFormat("namespaces"));
        }

        if (!"app".equals(graphType) && !"versionedApp".equals(graphType)) {
            return apiReturn(ApiPlaneErrorCode.InvalidFormat("graphType"));
        }

        if (StringUtils.isEmpty(duration) || !Pattern.matches(DURATION_PATTERN, duration)) {
            return apiReturn(ApiPlaneErrorCode.InvalidFormat("duration"));
        }

        Graph graph = topoService.getAppGraph(namespaces, duration, graphType, injectServices, focalizeApps, focalizeSize);
        return apiReturn(ImmutableMap.of(RESULT, graph.getElements()));
    }

    @RequestMapping(params = "Action=NotifySidecarEvent", method = RequestMethod.GET)
    public String notifySidecarDownload(@RequestParam(name = "SidecarVersion") String version,
                                        @RequestParam(name = "Type") String type){
        serviceMeshService.notifySidecarFileEvent(version, type);
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=CheckPilotHealth", method = RequestMethod.GET)
    public String checkPilotHealth() {
        boolean isHealth = serviceMeshService.checkPilotHealth();
        return apiReturn(ImmutableMap.of(RESULT, isHealth));
    }

    @RequestMapping(params = {"Action=RemoveSidecar"}, method = RequestMethod.GET)
    public String injectSidecar(@RequestParam(name = "Name") String name,
                                @RequestParam(name = "Namespace") String namespace,
                                @RequestParam(name = "Kind") String kind,
                                @RequestParam(name = "ClusterId") String clusterId) {

        ErrorCode code = serviceMeshService.removeInject(clusterId, kind, namespace, name);
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), null);
    }

    @RequestMapping(params = {"Action=GetProjectCodeByApp"}, method = RequestMethod.GET)
    public String getProjectCodeByApp(@RequestParam(name = "AppName") String name,
                                @RequestParam(name = "Namespace") String namespace,
                                @RequestParam(name = "ClusterId",required = false) String clusterId) {

        String projectCode = serviceMeshService.getProjectCodeByApp(namespace, name, clusterId);
        Map<String, Object> result = new HashMap<>();
        result.put("Result", projectCode);
        ErrorCode code = ApiPlaneErrorCode.Success;
        return apiReturn(code.getStatusCode(), code.getCode(), code.getMessage(), result);
    }

    @RequestMapping(params = {"Action=Validate"}, method = RequestMethod.POST)
    public String validateResource(@RequestBody String resource) {

        String unescapeRes = StringEscapeUtils.unescapeJava(resource);
        String json = optimize(unescapeRes);
        ValidateResult validate = validateService.validate(Arrays.asList(CommonUtil.json2HasMetadata(json)));
        ValidateResultDTO validateResultDTO = Trans.validateResult2ValidateResultDTO(validate);
        return apiReturn(ImmutableMap.of(RESULT, validateResultDTO));
    }

    @RequestMapping(params = "Action=GetPodLogs", method = RequestMethod.GET)
    public String getPodLogs(@RequestParam(value = "ClusterId", required = false) String clusterId,
                             @RequestParam(value = "Namespace") String namespace,
                             @RequestParam(value = "Pod") String pod,
                             @RequestParam(value = "Container", required = false) String container,
                             @RequestParam(value = "TailLines", required = false) @Min(1) @Max(100000) Integer tailLines,
                             @RequestParam(value = "SinceSeconds", required = false) @Min(1) @Max(86400) Long sinceSeconds) {

        String logs = serviceMeshService.getLogs(clusterId, namespace, pod, container, tailLines, sinceSeconds);
        return apiReturnInSilent(ImmutableMap.of(RESULT, StringUtils.isEmpty(logs) ? "" : logs));
    }

    @RequestMapping(params = "Action=UpdateNamespaceBinding", method = RequestMethod.GET)
    public String updateNamespaceBinding(@RequestParam(value = "ClusterId", required = false) String clusterId,
                                         @RequestParam(value = "Namespace") String namespace,
                                         @RequestParam(value = "IstioVersion") String version,
                                         @RequestParam(value = "Type", required = false, defaultValue = "istio-env") String type) {

        serviceMeshService.changeIstioVersion(clusterId, namespace, type, version);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(params = "Action=GetNamespaceBindings", method = RequestMethod.GET)
    public String getNamespaceBindings(@RequestParam(value = "ClusterId", required = false) String clusterId) {
        return apiReturn(ImmutableMap.of(RESULT, serviceMeshService.getIstioVersionBindings(clusterId)));
    }

    private String optimize(String json) {
        if (json.startsWith("\"") && json.startsWith("\"")) {
            json = json.substring(1, json.length() - 1);
        }
        return json;
    }
}
