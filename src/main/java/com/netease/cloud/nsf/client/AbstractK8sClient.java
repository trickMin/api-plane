package com.netease.cloud.nsf.client;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
public abstract class AbstractK8sClient<T extends HasMetadata> implements K8sResourceClient<T> {
    @Autowired
    private KubernetesClient kubernetesClient;

    protected KubernetesClient client() {
        return kubernetesClient;
    }

}
