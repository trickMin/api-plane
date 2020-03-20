package com.netease.cloud.nsf.core.gateway.service.impl;

import com.netease.cloud.nsf.core.GlobalConfig;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.service.ConfigStore;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 使用k8s作为存储
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Component
@Primary
public class K8sConfigStore implements ConfigStore {

    @Autowired
    KubernetesClient client;

    @Autowired
    GlobalConfig globalConfig;

    List<Class<? extends HasMetadata>> globalCrds = Arrays.asList(GatewayPlugin.class);

    @Override
    public void delete(HasMetadata resource) {
        supply(resource);
        HasMetadata r = resource;
        ObjectMeta meta = r.getMetadata();
        client.delete(r.getKind(), meta.getNamespace(), meta.getName());
    }

    @Override
    public void update(HasMetadata resource) {
        supply(resource);
        client.createOrUpdate(resource, ResourceType.OBJECT);
    }

    @Override
    public HasMetadata get(HasMetadata resource) {
        supply(resource);
        HasMetadata r = resource;
        ObjectMeta meta = r.getMetadata();
        return get(r.getKind(), meta.getNamespace(), meta.getName());
    }

    @Override
    public HasMetadata get(String kind, String namespace, String name) {
        return client.getObject(kind, namespace, name);
    }

    @Override
    public List<HasMetadata> get(String kind, String namespace) {
        return client.getObjectList(kind, namespace);
    }

    @Override
    public List<HasMetadata> get(String kind, String namespace, Map<String, String> labels) {
        return client.getObjectList(kind, namespace, labels);
    }

    void supply(HasMetadata resource) {
        if (isGlobalCrd(resource)) return;
        if (StringUtils.isEmpty(resource.getMetadata().getNamespace())) {
            resource.getMetadata().setNamespace(globalConfig.getResourceNamespace());
        }
    }

    boolean isGlobalCrd(HasMetadata resource) {
        if (globalCrds.contains(resource.getClass())) return true;
        return false;
    }
}
