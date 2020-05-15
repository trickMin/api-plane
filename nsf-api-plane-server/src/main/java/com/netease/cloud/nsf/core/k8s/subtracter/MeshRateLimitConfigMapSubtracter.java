package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.core.editor.PathExpressionEnum;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/5/12
 **/
public class MeshRateLimitConfigMapSubtracter extends RateLimitConfigMapSubtracter {

    private String service;

    public MeshRateLimitConfigMapSubtracter(String service) {
        this.service = service;
    }

    @Override
    public String getPath() {
        return PathExpressionEnum.REMOVE_MESH_RATELIMIT_CONFIGMAP_BY_VALUE.translate(service);
    }
}
