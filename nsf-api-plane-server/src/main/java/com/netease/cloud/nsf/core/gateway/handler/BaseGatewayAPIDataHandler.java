package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.API;

import java.util.List;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class BaseGatewayAPIDataHandler extends APIDataHandler {

    private Boolean enableHttp10;

    public BaseGatewayAPIDataHandler(Boolean enableHttp10) {
        this.enableHttp10 = enableHttp10;
    }

    @Override
    List<TemplateParams> doHandle(TemplateParams baseParams, API api) {
        return api.getGateways().stream()
                .map(gw -> TemplateParams.instance()
                                .setParent(baseParams)
                                .put(API_GATEWAY, gw)
                                .put(GATEWAY_HTTP_10, enableHttp10)
                                .put(GATEWAY_NAME, buildGatewayName(api.getService(), gw)))
                .collect(Collectors.toList());

    }

    private String buildGatewayName(String service, String gw) {
        return gw;
    }
}
