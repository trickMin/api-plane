package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
public interface IstioResourceOperator<T extends IstioResource> {

    T merge(T old, T fresh);

//    T subtract(T old, String name);

    boolean adapt(String name);

    /**
     * 删除old list中与new list重复的元素，
     * 然后将new list的元素全部加入old list中
     * @param oldL
     * @param newL
     * @param eq
     * @return
     */
    default List mergeList(List oldL, List newL, Equals eq) {
        List removal = new ArrayList();
        List result = new ArrayList(oldL);
        if (!CollectionUtils.isEmpty(newL)) {
            if (CollectionUtils.isEmpty(oldL)) {
                return newL;
            } else {
                for (Object no : newL) {
                    for (Object oo : oldL) {
                        if (eq.apply(no, oo)) {
                            removal.add(oo);
                            break;
                        }
                    }
                }
                result.removeAll(removal);
                result.addAll(newL);
            }
        }
        return result;
    }

}
