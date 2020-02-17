package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import me.snowdrop.istio.api.IstioResource;

import java.util.List;

public interface ServiceMeshService {

    void updateIstioResource(String json);

    void deleteIstioResource(String json);

    IstioResource getIstioResource(String name, String namespace, String kind);

    ErrorCode sidecarInject(String clusterId, String kind, String namespace, String name, String version, String expectedVersion);

    void notifySidecarFileEvent(String sidecarVersion, String type);

    void createSidecarVersionCRD(String clusterId, String namespace, String kind, String name, String expectedVersion);

    void createMissingCrd(List podList, String workLoadType, String workLoadName, String clusterId, String namespace);

    boolean checkPilotHealth();
}
