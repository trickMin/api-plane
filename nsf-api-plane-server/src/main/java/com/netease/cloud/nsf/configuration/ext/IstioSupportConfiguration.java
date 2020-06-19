package com.netease.cloud.nsf.configuration.ext;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import org.springframework.context.annotation.Bean;

public class IstioSupportConfiguration {

    @Bean
    public KubernetesClient kubernetesClient(MultiClusterK8sClient mc) {
    	return mc.k8sClient(MultiClusterK8sClient.DEFAULT_CLUSTER_NAME);
    }

    @Bean("originalKubernetesClient")
    public io.fabric8.kubernetes.client.KubernetesClient originalKubernetesClient(MultiClusterK8sClient mc) {
        return mc.originalK8sClient(MultiClusterK8sClient.DEFAULT_CLUSTER_NAME);
    }

    @Bean
    public MultiClusterK8sClient multiClusterK8sClient(K8sMultiClusterProperties properties, EditorContext editorContext) {
        return new MultiClusterK8sClient(properties, editorContext);
    }
}

