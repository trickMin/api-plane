package com.netease.cloud.nsf.core.operator;

import com.google.common.collect.ImmutableSet;
import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
public interface IstioResourceOperator<T extends IstioResource> {

    T merge(T old, T fresh);

    boolean adapt(String name);

    default List mergeList(List oldL, List newL, Equals eq) {
        List extra = new ArrayList<>();
        if (!CollectionUtils.isEmpty(newL)) {
            if (CollectionUtils.isEmpty(oldL)) {
                return newL;
            } else {
                for (Object no : newL) {
                    for (Object oo : oldL) {
                        if (eq.apply(no, oo)) break;
                    }
                    extra.add(no);
                }
                extra.addAll(oldL);
            }
        }
        return extra;
    }

}
