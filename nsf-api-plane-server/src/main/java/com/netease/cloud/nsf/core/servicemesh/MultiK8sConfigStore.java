package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.GlobalConfig;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.service.impl.K8sConfigStore;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/5
 **/
@Component
public class MultiK8sConfigStore extends K8sConfigStore {

    private MultiClusterK8sClient multiClient;

    @Autowired
    public MultiK8sConfigStore(MultiClusterK8sClient multiClient, KubernetesClient client, GlobalConfig globalConfig) {
        super(client, globalConfig);
        this.multiClient = multiClient;
    }

    public void update(HasMetadata t, String clusterId) {
        resolve(clusterId).createOrUpdate(t, ResourceType.OBJECT);
    }

    public void delete(HasMetadata t, String clusterId) {
        resolve(clusterId).delete(t.getKind(), t.getMetadata().getNamespace(), t.getMetadata().getName());
    }

    public HasMetadata get(String kind, String namespace, String name, String clusterId) {
        KubernetesClient k8sClient = resolve(clusterId);
        if (k8sClient == null) {
            return null;
        }
        return k8sClient.getObject(kind, namespace, name);
    }

    public List<HasMetadata> getList(String kind, String namespace, String clusterId) {
        return resolve(clusterId).getObjectList(kind, namespace);
    }

    private KubernetesClient resolve(String clusterId) {
        return multiClient.k8sClient(clusterId);
    }

    @Override
    public void update(HasMetadata resource) {
        resolve(getDefaultClusterId()).createOrUpdate(resource, ResourceType.OBJECT);
    }

    public String getPodLog(String clusterId, String pod, String namespace, String container, Integer tailLines, Long sinceSeconds) {
        clusterId = StringUtils.isEmpty(clusterId) ? getDefaultClusterId() : clusterId;
        StringBuilder urlBuilder = new StringBuilder();
        KubernetesClient client = resolve(clusterId);

        urlBuilder.append(client.getMasterUrl());
        urlBuilder.append(String.format("api/v1/namespaces/%s/pods/%s/log?1=1", namespace, pod));
        if (!StringUtils.isEmpty(container)) {
            urlBuilder.append(String.format("&container=%s", container));
        }
        if (tailLines != null) {
            urlBuilder.append(String.format("&tailLines=%d", tailLines));
        }
        if (sinceSeconds != null) {
            urlBuilder.append(String.format("&sinceSeconds=%d", sinceSeconds));
        }
        return client.getInSilent(urlBuilder.toString());
    }

    public String getDefaultClusterId() {
        return multiClient.DEFAULT_CLUSTER_NAME;
    }
}
