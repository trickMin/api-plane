package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.GlobalConfig;
import com.netease.cloud.nsf.core.gateway.service.impl.K8sConfigStore;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import io.fabric8.kubernetes.api.model.HasMetadata;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/8/7
 **/
public class MeshK8sConfigStore extends K8sConfigStore {
    public MeshK8sConfigStore(KubernetesClient client, GlobalConfig globalConfig) {
        super(client, globalConfig);
    }

    @Override
    protected void supply(HasMetadata resource) {
        //DO NOTHING
    }
}
