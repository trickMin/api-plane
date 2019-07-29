package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.meta.APIModel;
import com.netease.cloud.nsf.meta.ApiOption;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
@Component
public class GatewayModelProcessor {

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
        if (old == null) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
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
