package com.netease.cloud.nsf.core.k8s.validator;

import com.netease.cloud.nsf.util.Const;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.Set;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
public interface K8sResourceValidator<T extends HasMetadata> {
    boolean adapt(String name);

    Set<ConstraintViolation<T>> validate(T var);
}
