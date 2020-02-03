package com.netease.cloud.nsf.core.istio.function;

import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;

import java.util.function.Function;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/22
 **/
public class VirtualServiceFunction implements ResourceFunction<VirtualService> {


    Function<VirtualService, VirtualService> delete() {

        return virtualService -> null;

    }

    @Override
    public VirtualService apply(VirtualService virtualService) {
        return null;
    }

    @Override
    public boolean match(Object o) {
        return VirtualService.class.equals(o.getClass());
    }
}
