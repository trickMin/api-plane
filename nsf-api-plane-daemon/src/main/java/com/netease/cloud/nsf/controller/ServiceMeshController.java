package com.netease.cloud.nsf.controller;

import com.netease.cloud.nsf.service.SidecarFileDownloadService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/servicemesh", params = "Version=2019-01-02")
public class ServiceMeshController extends BaseController {

    @Autowired
    private SidecarFileDownloadService downloadService;

    @RequestMapping(params = {"Action=EnvoyEvent"}, method = RequestMethod.GET)
    public String DownloadEnvoy(@RequestParam(name = "SidecarVersion") String sidecarVersion,
                                @RequestParam(name = "Type") String type) {
        if (type == Const.DOWNLOAD_ENVOY_EVENT) {
            downloadService.downloadSidecar(sidecarVersion);
        }else if (type == Const.DELETE_ENVOY_EVENT){
            downloadService.deleteSidecar(sidecarVersion);
        }
        return apiReturn(ApiPlaneErrorCode.Success);
    }


}
