package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.meta.WhiteList;
import com.netease.cloud.nsf.service.WhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/26
 **/
@RestController
@RequestMapping(value = "/api/istio/rbac", params = "Version=2018-05-31")
public class WhiteListController extends BaseController {
    @Autowired
    private WhiteListService whiteListService;

    @RequestMapping(params = "Action=Enable", method = RequestMethod.POST)
    public String enable(@RequestBody WhiteList whiteList) {
        whiteListService.initResource(whiteList);
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=Update", method = RequestMethod.POST)
    public String update(@RequestBody WhiteList whiteList) {
        whiteListService.updateService(whiteList);
        return apiReturn(SUCCESS, "Success", null, null);
    }

    @RequestMapping(params = "Action=Delete", method = RequestMethod.POST)
    public String remove(@RequestBody WhiteList whiteList) {
        whiteListService.removeService(whiteList);
        return apiReturn(SUCCESS, "Success", null, null);
    }
}
