package com.netease.cloud.nsf.core.k8s;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
@Component
public class IntegratedClient {
    private static final Logger logger = LoggerFactory.getLogger(IntegratedClient.class);

    @Autowired
    private List<K8sResourceClient> k8sResourceClients;

    public void createOrUpdate(List<HasMetadata> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            logger.warn("createOrUpdate failed. empty resources list");
            return;
        }
        resources.stream().forEach(r -> createOrUpdate(r));
    }

    public void createOrUpdate(HasMetadata resource) {
        resolve(resource.getKind()).createOrUpdate(resource);
    }

    public void deleteByName(String name, String namespace, String type) {
        resolve(type).delete(type, name, namespace);
    }

    public HasMetadata get(String name, String namespace, String type) {
        return resolve(type).get(type, name, namespace);
    }

    public List<HasMetadata> getResources(String namespace, String type) {
        return resolve(type).getList(type, namespace);
    }

    private K8sResourceClient resolve(String type) {
        for (K8sResourceClient client : k8sResourceClients) {
            if (client.isAdapt(type)) {
                return client;
            }
        }
        throw new RuntimeException("cannot resolve the suitable istio resource client");
    }
}
