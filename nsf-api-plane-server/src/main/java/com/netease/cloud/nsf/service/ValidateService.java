package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.ValidateResult;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
public interface ValidateService {
    ValidateResult validate(List<HasMetadata> resources);
}
