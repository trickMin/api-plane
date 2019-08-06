package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.meta.WhiteList;
import com.netease.cloud.nsf.service.WhiteListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/26
 **/
@RestController
@RequestMapping(value = "/api/istio/rbac", params = "Version=2018-05-31")
public class WhiteListController extends BaseController {
    @Autowired
    private WhiteListService whiteListService;

    @RequestMapping(params = "Action=Update", method = RequestMethod.POST)
    public String update(WhiteList whiteList, @RequestHeader("X-Forwarded-Client-Cert") String certHeader) {
        if (!resolveRequestCert(whiteList, certHeader)) {
            return apiReturn(401, "UnAuthorized", String.format("UnAuthorized, X-Forwarded-Client-Cert: %s", certHeader), null);
        }
    	if (whiteList.getService() == null || whiteList.getService().equals("")) {
            whiteListService.updateService(whiteList);
        }
        return apiReturn(SUCCESS, "Success", null, null);
    }

    private boolean resolveRequestCert(WhiteList whiteList, String header) {
        if (header == null || header.equals("")) {
            return false;
        }
        String[] spiltValues = header.split(";");
        String uri = null;
        for (String spiltValue : spiltValues) {
            Pattern pattern = Pattern.compile("(.*)=(.*)");
            Matcher matcher = pattern.matcher(spiltValue);
            if (matcher.find()) {
                String k = matcher.group(1);
                String v = matcher.group(2);
                if (k.equalsIgnoreCase("uri")) {
                    uri = v;
                    break;
                }
            }
        }
        if (uri == null || uri.equals("")) {
            return false;
        }
        Pattern pattern = Pattern.compile("spiffe://(.*)/ns/(.*)/sa/(.*)");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            whiteList.setNamespace(matcher.group(2));
            whiteList.setService(matcher.group(3));
        }
        return true;
    }

}
