package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.K8sResourceValidateException;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
public interface ValidateService {
    void validate(List<HasMetadata> resources) throws K8sResourceValidateException;
}
