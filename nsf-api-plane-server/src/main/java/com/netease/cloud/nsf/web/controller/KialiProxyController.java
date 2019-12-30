package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.core.kiali.KialiHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * kiali代理接口
 * Created by 张武 at 2019/12/25
 */
@Controller
@RequestMapping(KialiProxyController.KIALI_PATH_PREFIX)
public class KialiProxyController extends BaseController {

    public static final String KIALI_PATH_PREFIX = "/api/kiali";

    @Autowired
    private KialiHttpClient kialiHttpClient;

    @RequestMapping("/**")
    @ResponseBody
    String redirectToKiali(HttpServletRequest request, @RequestBody(required = false) String body) {
        String uri = request.getRequestURI().replaceAll("^" + KIALI_PATH_PREFIX, "/kiali");
        if (request.getQueryString() != null) {
            uri += "?" + request.getQueryString();
        }
        HttpEntity<String> entity = kialiHttpClient.redirectToKiali(HttpMethod.resolve(request.getMethod()), uri, body);
        return entity.getBody();
    }

}
