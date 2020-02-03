package com.netease.cloud.nsf.core.istio.function;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/1/22
 **/
public class FunctionDispatcher {


    private List<ResourceFunction> funs = new ArrayList<>();

    public FunctionDispatcher(List<ResourceFunction> funs) {
        if (funs != null) {
            this.funs.addAll(funs);
        }
    }

    private <T> ResourceFunction dispatch(T t) {

        for (ResourceFunction fun : funs) {
            if (fun.match(t)) {
                return fun;
            }
        }
        return doNothing;
    }


    private ResourceFunction doNothing = new ResourceFunction() {
        @Override
        public Object apply(Object o) {
            return o;
        }


        // match all type
        @Override
        public boolean match(Object o) {
            return true;
        }
    };

}
