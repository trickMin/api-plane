package com.netease.cloud.nsf.core.operator;

import com.google.common.collect.ImmutableSet;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
public interface IstioResourceOperator<T extends IstioResource> {

    T merge(T old, T fresh);

    boolean adapt(String name);

    default List mergeList(List oldL, List newL) {
        if (!CollectionUtils.isEmpty(newL)) {
            if (CollectionUtils.isEmpty(oldL)) {
                return newL;
            } else {
                return ImmutableSet.of(oldL, newL).asList();
            }
        }
        return oldL;
    }
}
