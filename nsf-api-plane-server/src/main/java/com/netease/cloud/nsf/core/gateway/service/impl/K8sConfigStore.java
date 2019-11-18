package com.netease.cloud.nsf.core.gateway.service.impl;

import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.service.ConfigStore;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 使用k8s作为存储
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Component
public class K8sConfigStore implements ConfigStore {

    @Autowired
    KubernetesClient client;

    @Value("${resourceNamespace:default}")
    private String resourceNamespace;

    @Override
    public void create(IstioResource istioResource) {
        update(istioResource);
    }

    @Override
    public void delete(IstioResource istioResource) {
        supply(istioResource);
        IstioResource r = istioResource;
        ObjectMeta meta = r.getMetadata();
        client.delete(r.getKind(), meta.getNamespace(), meta.getName());
    }

    @Override
    public void update(IstioResource istioResource) {
        supply(istioResource);
        client.createOrUpdate(istioResource, ResourceType.OBJECT);
    }


    @Override
    public IstioResource get(IstioResource istioResource) {
        supply(istioResource);
        IstioResource r = istioResource;
        ObjectMeta meta = r.getMetadata();
        return get(r.getKind(), meta.getNamespace(), meta.getName());
    }

    @Override
    public IstioResource get(String kind, String namespace, String name) {
        return client.getObject(kind, namespace, name);
    }

    @Override
    public List<IstioResource> get(String kind, String namespace) {
        return client.getObjectList(kind, namespace);
    }

    @Override
    public List<IstioResource> get(String kind, String namespace, Map<String, String> labels) {
        return client.getObjectList(kind, namespace, labels);
    }

    @Override
    public boolean exist(IstioResource t) {
        return get(t) != null;
    }

    private void supply(IstioResource istioResource) {
        if (StringUtils.isEmpty(istioResource.getMetadata().getNamespace())) {
            istioResource.getMetadata().setNamespace(resourceNamespace);
        }
    }
}
