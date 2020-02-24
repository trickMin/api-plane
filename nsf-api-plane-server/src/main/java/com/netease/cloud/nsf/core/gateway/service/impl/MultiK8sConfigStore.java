package com.netease.cloud.nsf.core.gateway.service.impl;

import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/5
 **/
@Component
public class MultiK8sConfigStore extends K8sConfigStore {

    @Autowired
    private MultiClusterK8sClient multiClient;

    public void update(HasMetadata t, String clusterId) {
        supply(t);
        resolve(clusterId).createOrUpdate(t, ResourceType.OBJECT);
    }

    public HasMetadata get(String kind, String namespace, String name, String clusterId) {
        return resolve(clusterId).getObject(kind, namespace, name);
    }

    private KubernetesClient resolve(String clusterId) {
        return multiClient.k8sClient(clusterId);
    }
}
