package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.util.errorcode.ErrorCode;

public interface ServiceMeshService {

    void updateIstioResource(String json);

    void deleteIstioResource(String json);

    ErrorCode sidecarInject(String clusterId, String kind, String namespace, String name, String version, String expectedVersion);
}
