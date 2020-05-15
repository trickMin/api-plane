package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.core.editor.PathExpressionEnum;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/5/12
 **/
public class GatewayRateLimitConfigMapSubtracter extends RateLimitConfigMapSubtracter {

    private String gateway;
    private String api;

    public GatewayRateLimitConfigMapSubtracter(String gateway, String api) {
        this.gateway = gateway;
        this.api = api;
    }

    @Override
    public String getPath() {
        return PathExpressionEnum.REMOVE_GATEWAY_RATELIMIT_CONFIGMAP_BY_VALUE.translate(gateway, api);
    }
}
