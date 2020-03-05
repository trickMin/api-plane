package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.IstioResource;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2020/3/5
 **/
public interface ValidateService {
    void validate(List<HasMetadata> resources) throws ApiPlaneException;
}
