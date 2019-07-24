package com.netease.cloud.nsf.meta;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.api.model.Doneable;


/**
 *
 * 当replace完成时会调用done方法，create不会调用
 * 默认只是替换Resource的Metadata.ResourceVersion
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/16
 **/
public class DoneableIstioResource implements Doneable<IstioResource> {
    private final IstioResource istioResource;
    private final Function<IstioResource, IstioResource> function;

    public DoneableIstioResource(Function<IstioResource, IstioResource> function) {
        this.function = function;
        this.istioResource = new IstioResource();
    }

    public DoneableIstioResource(IstioResource istioResource) {
        this.istioResource = istioResource;
        this.function = new Function<IstioResource, IstioResource>() {
            @Override
            public IstioResource apply(IstioResource istioResource) {
                return istioResource;
            }
        };
    }

    public DoneableIstioResource(IstioResource istioResource, Function<IstioResource, IstioResource> function) {
        this.istioResource = istioResource;
        this.function = function;
    }

    @Override
    public IstioResource done() {
        return function.apply(istioResource);
    }
}
