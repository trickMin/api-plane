package com.netease.cloud.nsf.core.k8s;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import me.snowdrop.istio.client.IstioClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
public abstract class AbstractK8sClient<T extends HasMetadata> implements K8sResourceClient<T> {
    @Autowired
    private KubernetesClient kubernetesClient;

    protected KubernetesClient k8sClient() {
        return kubernetesClient;
    }

    @Autowired
    private IstioClient istioClient;

    protected IstioClient istioClient() {
        return istioClient;
    }
}
