package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.service.impl.K8sConfigStore;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/5
 **/
@Component
public class MultiK8sConfigStore extends K8sConfigStore {

    private MultiClusterK8sClient multiClient;

    @Autowired
    public MultiK8sConfigStore(MultiClusterK8sClient multiClient) {
        this.multiClient = multiClient;
    }

    public void update(HasMetadata t, String clusterId) {
        supply(t);
        resolve(clusterId).createOrUpdate(t, ResourceType.OBJECT);
    }

    public HasMetadata get(String kind, String namespace, String name, String clusterId) {
        return resolve(clusterId).getObject(kind, namespace, name);
    }

    public List<HasMetadata> getList(String kind, String namespace, String clusterId) {
        return resolve(clusterId).getObjectList(kind, namespace);
    }

    private KubernetesClient resolve(String clusterId) {
        return multiClient.k8sClient(clusterId);
    }

    @Override
    public void update(HasMetadata resource) {
        supply(resource);
        resolve(getDefaultClusterId()).createOrUpdate(resource, ResourceType.OBJECT);
    }

    private String getDefaultClusterId() {
        return multiClient.DEFAULT_CLUSTER_NAME;
    }
}
