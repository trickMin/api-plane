package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/19
 **/
@RestController
@RequestMapping(value = "/api", params = "Version=2019-07-25")
public class CommonAPIController extends BaseController{

    @RequestMapping(params = "Action=DeleteAPI", method = RequestMethod.GET)
    public String deleteApi() {
        return apiReturn(ApiPlaneErrorCode.Success);
    }


}
