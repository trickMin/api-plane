package com.netease.cloud.nsf.service;

public interface ServiceMeshService {

    void updateIstioResource(String json);

    void deleteIstioResource(String json);
}
