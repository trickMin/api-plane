package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.ConfigStore;
import com.netease.cloud.nsf.core.GlobalConfig;
import com.netease.cloud.nsf.core.gateway.service.impl.K8sConfigStore;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/5
 **/
@Component
public class MultiK8sConfigStore extends K8sConfigStore {

    private MultiClusterK8sClient multiClient;
    private Map<String, K8sConfigStore> k8sConfigStores = new HashMap<>();

    @Autowired
    public MultiK8sConfigStore(MultiClusterK8sClient multiClient, KubernetesClient client, GlobalConfig globalConfig) {
        super(client, globalConfig);
        this.multiClient = multiClient;
        if (multiClient != null && multiClient.getAllClients() != null && !multiClient.getAllClients().isEmpty()) {
            Map<String, MultiClusterK8sClient.ClientSet> clients = multiClient.getAllClients();
            clients.forEach((k, c) -> {
                k8sConfigStores.put(k, new K8sConfigStore(c.k8sClient, globalConfig));
            });
        }
    }

    public void update(HasMetadata t, String clusterId) {
        resolve(clusterId).update(t);
    }

    public void delete(HasMetadata t, String clusterId) {
        resolve(clusterId).delete(t);
    }

    public HasMetadata get(String kind, String namespace, String name, String clusterId) {
        ConfigStore configStore = resolve(clusterId);
        if (configStore == null) {
            return null;
        }
        return configStore.get(kind, namespace, name);
    }

    public List<HasMetadata> getList(String kind, String namespace, String clusterId) {
        return resolve(clusterId).get(kind, namespace);
    }

    public ConfigStore resolve(String clusterId) {
        return k8sConfigStores.get(clusterId);
    }

    @Override
    public HasMetadata get(HasMetadata resource) {
        return get(resource, getDefaultClusterId());
    }

    public HasMetadata get(HasMetadata resource, String clusterId) {
        ObjectMeta meta = resource.getMetadata();
        return resolve(clusterId).get(resource.getKind(), meta.getNamespace(), meta.getName());
    }

    @Override
    public void update(HasMetadata resource) {
       update(resource, getDefaultClusterId());
    }

    public String getPodLog(String clusterId, String pod, String namespace, String container, Integer tailLines, Long sinceSeconds) {
        clusterId = StringUtils.isEmpty(clusterId) ? getDefaultClusterId() : clusterId;
        StringBuilder urlBuilder = new StringBuilder();
        KubernetesClient client = multiClient.getAllClients().get(clusterId).k8sClient;

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
