package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.meta.web.PortalAPI;
import com.netease.cloud.nsf.meta.web.PortalService;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.Trans;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/20
 **/

@RestController
@RequestMapping(value = "/api", params = "Version=2019-07-25")
public class PortalGatewayController extends BaseController {

    @Autowired
    private GatewayService gatewayService;

    @RequestMapping(value = "/portal", params = "Action=PublishAPI", method = RequestMethod.POST)
    public String publishPortalAPI(@RequestBody @Valid PortalAPI api) {
        gatewayService.updateAPI(Trans.portalAPI2API(api));
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/portal", params = "Action=PublishService", method = RequestMethod.POST)
    public String publishPortalService(@RequestBody @Valid PortalService service) {
        gatewayService.updateService(service);
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/portal", params = "Action=DeleteAPI", method = RequestMethod.POST)
    public String deletePortalAPI(@RequestBody @Valid PortalAPI api) {
        gatewayService.deleteAPI(Trans.portalAPI2API(api));
        return apiReturn(ApiPlaneErrorCode.Success);
    }

    @RequestMapping(value = "/portal", params = "Action=DeleteService", method = RequestMethod.POST)
    public String deletePortalService(@RequestBody @Valid PortalService service) {
        gatewayService.deleteService(service);
        return apiReturn(ApiPlaneErrorCode.Success);
    }
}
