package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.meta.APIModel;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
@Component
public class ModelProcessor {

    /**
     * 将api转换为istio对应的规则
     * @param api
     * @return
     */
    public List<IstioResource> translate(APIModel api) {
        return null;
    }

    /**
     * 合并两个crd
     * @param old
     * @param fresh
     * @return
     */
    public IstioResource merge(IstioResource old, IstioResource fresh) {
        if (old == null) throw new RuntimeException("istio resource is non-exist");
        if (fresh == null) return old;

        // TODO
        return old;
    }

    /**
     * 在已有的istio crd中删去对应api部分
     * @param old
     * @param api
     * @return
     */
    public IstioResource subtract(IstioResource old, String api) {

        // TODO

        return old;
    }
}
