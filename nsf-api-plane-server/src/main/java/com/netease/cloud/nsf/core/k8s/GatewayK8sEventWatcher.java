package com.netease.cloud.nsf.core.k8s;


import com.google.common.collect.ImmutableSet;
import com.netease.cloud.nsf.core.k8s.event.K8sResourceDeleteNotificationEvent;
import com.netease.cloud.nsf.core.slime.SlimeHttpClient;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.Set;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/6/5
 **/
public class GatewayK8sEventWatcher {

    final static Set<String> SUPPORTED_RESOURCES = ImmutableSet.of(K8sResourceEnum.VirtualService.name(),
            K8sResourceEnum.DestinationRule.name(), K8sResourceEnum.GatewayPlugin.name());

    SlimeHttpClient slimeClient;

    public GatewayK8sEventWatcher(SlimeHttpClient slimeClient) {
        this.slimeClient = slimeClient;
    }

    @Async
    @EventListener
    public void notifySlimeDeletion(K8sResourceDeleteNotificationEvent event) {
        HasMetadata resource = event.getHmd();
        if (SUPPORTED_RESOURCES.contains(resource.getKind())) {
            slimeClient.notifyDeletion(resource.getKind(), resource.getMetadata().getName(), resource.getMetadata().getNamespace());
        }
    }

}
