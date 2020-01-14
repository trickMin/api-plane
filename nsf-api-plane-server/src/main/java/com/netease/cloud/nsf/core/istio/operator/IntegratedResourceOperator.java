package com.netease.cloud.nsf.core.istio.operator;

import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
@Component
public class IntegratedResourceOperator {

    @Autowired
    private List<IstioResourceOperator> operators;

    public IstioResource merge(IstioResource old, IstioResource fresh) {

        if (old == null || fresh == null) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        if (!identical(old, fresh)) throw new ApiPlaneException(ExceptionConst.RESOURCES_DIFF_IDENTITY);
        return resolve(old).merge(old, fresh);
    }

    public IstioResource subtract(IstioResource old, String value) {

        if (old == null) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);

        return resolve(old).subtract(old, value);
    }

    private boolean identical(IstioResource old, IstioResource fresh) {
        return old.getKind().equals(fresh.getKind()) &&
                old.getMetadata().getNamespace().equals(fresh.getMetadata().getNamespace()) &&
                old.getMetadata().getName().equals(fresh.getMetadata().getName());
    }

    public boolean isUseless(IstioResource i) {
        return resolve(i).isUseless(i);
    }

    private IstioResourceOperator resolve(IstioResource i) {
        for (IstioResourceOperator op : operators) {
            if (op.adapt(i.getKind())) {
                return op;
            }
        }
        throw new ApiPlaneException(ExceptionConst.UNSUPPORTED_RESOURCE_TYPE + ":" + i.getKind());
    }
}
