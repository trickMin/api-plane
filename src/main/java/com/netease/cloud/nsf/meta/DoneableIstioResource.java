package com.netease.cloud.nsf.meta;

import io.fabric8.kubernetes.api.model.Doneable;

/**
 * todo:
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/16
 **/
public class DoneableIstioResource implements Doneable<IstioResource> {
    @Override
    public IstioResource done() {
        return null;
    }
}
