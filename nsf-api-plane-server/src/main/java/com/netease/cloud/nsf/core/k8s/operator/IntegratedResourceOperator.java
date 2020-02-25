package com.netease.cloud.nsf.core.k8s.operator;

import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.HasMetadata;
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
    private List<k8sResourceOperator> operators;

    public HasMetadata merge(HasMetadata old, HasMetadata fresh) {

        if (old == null || fresh == null) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);
        if (!identical(old, fresh)) throw new ApiPlaneException(ExceptionConst.RESOURCES_DIFF_IDENTITY);
        return resolve(old).merge(old, fresh);
    }

    public HasMetadata subtract(HasMetadata old, String value) {

        if (old == null) throw new ApiPlaneException(ExceptionConst.RESOURCE_NON_EXIST);

        return resolve(old).subtract(old, value);
    }

    private boolean identical(HasMetadata old, HasMetadata fresh) {

        boolean sameKind = old.getKind().equals(fresh.getKind());
        boolean sameNamespace;
        // 全局配置
        if (old.getMetadata().getNamespace() == null) {
            sameNamespace = true;
        } else {
            sameNamespace = old.getMetadata().getNamespace().equals(fresh.getMetadata().getNamespace());
        }
        boolean sameName = old.getMetadata().getName().equals(fresh.getMetadata().getName());
        return sameKind && sameNamespace && sameName;
    }

    public boolean isUseless(HasMetadata i) {
        return resolve(i).isUseless(i);
    }

    private k8sResourceOperator resolve(HasMetadata i) {
        for (k8sResourceOperator op : operators) {
            if (op.adapt(i.getKind())) {
                return op;
            }
        }
        throw new ApiPlaneException(ExceptionConst.UNSUPPORTED_RESOURCE_TYPE + ":" + i.getKind());
    }
}