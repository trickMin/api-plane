package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.APIModel;
import com.netease.cloud.nsf.meta.IstioResource;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
public class ModelProcessor {

    public <T> List<T> translate(APIModel api) {
        return null;
    }

    public IstioResource merge(IstioResource old, IstioResource fresh) {
        if (old == null) throw new RuntimeException("istio resource is non-exist");
        if (fresh == null) return old;

        // TODO
        return old;
    }
}
