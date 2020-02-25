package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.dto.ResourceWrapperDTO;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

public interface ServiceMeshService {

    void updateIstioResource(String json);

    void deleteIstioResource(String json);

    HasMetadata getIstioResource(String name, String namespace, String kind);

    List<ResourceWrapperDTO> getIstioResourceList(String namespaces, String kind);

    ErrorCode sidecarInject(String clusterId, String kind, String namespace, String name, String version, String expectedVersion);

    void notifySidecarFileEvent(String sidecarVersion, String type);

    void createSidecarVersionCRD(String clusterId, String namespace, String kind, String name, String expectedVersion);

    void createMissingCrd(List podList, String workLoadType, String workLoadName, String clusterId, String namespace);

    boolean checkPilotHealth();
}
