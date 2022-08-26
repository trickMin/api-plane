package org.hango.cloud.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import org.apache.commons.collections.CollectionUtils;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.gateway.service.impl.K8sConfigStore;
import org.hango.cloud.core.k8s.K8sResourceApiEnum;
import org.hango.cloud.core.k8s.MultiClusterK8sClient;
import org.hango.cloud.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @Author: zhufengwei.sx
* @Date: 2022/8/26 14:37
**/
@Component
public class K8sResourceCache implements ResourceCache {
    private static final Logger log = LoggerFactory.getLogger(K8sResourceCache.class);

    @Autowired
    private MultiClusterK8sClient multiClusterK8sClient;

    @Autowired
    private K8sConfigStore k8sConfigStore;

    @Autowired
    private GlobalConfig globalConfig;

    private Map<String, ResourceStore> storeMap = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void initInformer() {
        if (!multiClusterK8sClient.watchResource()){
            return;
        }
        startInformer(K8sResourceApiEnum.DestinationRule);
        startInformer(K8sResourceApiEnum.VirtualService);
        startInformer(K8sResourceApiEnum.ServiceEntry);
    }

    private void startInformer(K8sResourceApiEnum kind){
        KubernetesClient masterOriginalClient = multiClusterK8sClient.getMasterOriginalClient();
        CustomResourceDefinition crd;
        try {
            crd = masterOriginalClient.customResourceDefinitions().withName(kind.getApi()).get();
        } catch (Exception e) {
            log.error("get crd definition error", e);
            return;
        }
        RawCustomResourceOperationsImpl rawCustomResourceOperations = masterOriginalClient.customResource(CustomResourceDefinitionContext.fromCrd(crd));
        K8sResourceInformer mupInformer = new K8sResourceInformer
                .Builder()
                .addNamespace(globalConfig.getResourceNamespace())
                .addResourceKind(kind.name())
                .addResourceStore(storeMap.computeIfAbsent(kind.name(), k -> new ResourceStore()))
                .addRawCustomResourceOperationsImpl(rawCustomResourceOperations)
                .build();
        mupInformer.start();
    }



    @Override
    public List<HasMetadata> getResource(String kind) {
        if (!storeMap.containsKey(kind)){
            return new ArrayList<>();
        }
        return storeMap.get(kind).list();
    }

    @Override
    public void refresh(String kind, List<String> names) {
        if (CollectionUtils.isEmpty(names) || !storeMap.containsKey(kind)){
            log.error("refresh resource failed, kind:{}, name:{}}", kind, CommonUtil.toJSONString(names));
            return;
        }
        for (String name : names) {
            HasMetadata resource = k8sConfigStore.get(kind, globalConfig.getResourceNamespace(), name);
            storeMap.get(kind).update(name, resource, resource.getMetadata().getResourceVersion(), true);
        }
    }
}

