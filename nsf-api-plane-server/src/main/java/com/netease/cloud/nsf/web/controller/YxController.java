package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.meta.APIModel;
import com.netease.cloud.nsf.util.errorcode.ApiPlaneErrorCode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
@RestController
@RequestMapping(value = "/api/yx", params = "Version=2019-07-25")
public class YxController extends BaseController{


    @RequestMapping(params = "Action=PublishAPI", method = RequestMethod.POST)
    public String publishAPI(@Valid APIModel api) {

        return apiReturn(ApiPlaneErrorCode.Success);
    }
}
