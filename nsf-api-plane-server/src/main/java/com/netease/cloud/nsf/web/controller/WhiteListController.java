package com.netease.cloud.nsf.web.controller;

import com.netease.cloud.nsf.meta.WhiteList;
import com.netease.cloud.nsf.service.WhiteListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
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
    private static final Logger logger = LoggerFactory.getLogger(WhiteListController.class);

    @RequestMapping(params = "Action=Update", method = RequestMethod.POST)
    public String update(@RequestBody WhiteList whiteList, @RequestHeader(value = "X-Forwarded-Client-Cert", required = false) String certHeader) {
        if (!resolveRequestCert(whiteList, certHeader) || whiteList.getService() == null || whiteList.getService().equals("")) {
            return apiReturn(401, "UnAuthenticated", String.format("UnAuthenticated, X-Forwarded-Client-Cert: %s", certHeader), null);
        }
        if (whiteList.getService().startsWith("qz-") || whiteList.getNamespace().equals("istio-system")) {
            return apiReturn(403, "UnAuthorized", String.format("UnAuthorized, whitelist request not allowed from %s.%s", whiteList.getService(), whiteList.getNamespace()), null);
        }
        whiteListService.updateService(whiteList);
        return apiReturn(SUCCESS, "Success", null, null);
    }

    private boolean resolveRequestCert(WhiteList whiteList, String header) {
    	logger.info("resolving cert: '{}'", header);
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
        } else {
            return false;
        }
        logger.info("successfully resolved target service: {}.{}, cert: {}", whiteList.getNamespace(), whiteList.getService(), header);
        return true;
    }

}
