package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.API;

import java.util.List;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.API_GATEWAY;
import static com.netease.cloud.nsf.core.template.TemplateConst.GATEWAY_NAME;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class BaseGatewayAPIDataHandler extends APIDataHandler {

    @Override
    List<TemplateParams> doHandle(TemplateParams baseParams, API api) {
        return api.getGateways().stream()
                .map(gw -> TemplateParams.instance()
                                .setParent(baseParams)
                                .put(API_GATEWAY, gw)
                                .put(GATEWAY_NAME, buildGatewayName(api.getService(), gw)))
                .collect(Collectors.toList());

    }

    private String buildGatewayName(String service, String gw) {
        return gw;
    }
}
