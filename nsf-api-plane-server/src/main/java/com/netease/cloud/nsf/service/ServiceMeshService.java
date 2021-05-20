package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.dto.ResourceWrapperDTO;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ServiceMeshService {

    void updateIstioResource(String json, String clusterId);

    void deleteIstioResource(String json, String clusterId);

    HasMetadata getIstioResource(String clusterId, String name, String namespace, String kind);

    List<ResourceWrapperDTO> getIstioResourceList(String clusterId, String namespaces, String kind);

	String invokeIstiodApi(String clusterId, String podName, String namespace, String path);

    ErrorCode sidecarInject(String clusterId, String kind, String namespace, String name, String version, String expectedVersion, String appName);

    void notifySidecarFileEvent(String sidecarVersion, String type);

    void createSidecarVersionCRD(String clusterId, String namespace, String kind, String name, String expectedVersion);

    void createMissingCrd(List podList, String workLoadType, String workLoadName, String clusterId, String namespace);

    boolean checkPilotHealth();

    ErrorCode removeInject(String clusterId, String kind, String namespace, String name);

    Map<String, List<String>> getTrafficMarks(List<String> services);

    void updateNsfTrafficMarkAnnotations(Map<String, Set<String>> mappings);

    ErrorCode createAppOnService(String clusterId, String namespace, String name, String appName);

    String getProjectCodeByApp(String namespace, String appName, String clusterId);

    void updateDefaultSidecarVersion(String defaultSidecarVersion);

    String getLogs(String clusterId, String namespace, String podName, String container, Integer tailLines, Long sinceSeconds);

    void changeIstioVersion(String clusterId, String namespace, String type, String value);

    List<Map<String, String>> getIstioVersionBindings(String clusterId);

    Map<String, Map<String, String>> getSyncz(String type, String version);

    List<String> getIstiodInstances(String type, String version);
}
