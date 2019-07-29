package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.core.k8s.IntegratedClient;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Component
public class K8sConfigStore implements ConfigStore {

    @Autowired
    IntegratedClient client;

    @Override
    public void create(IstioResource istioResource) {
        update(istioResource);
    }

    @Override
    public void delete(IstioResource istioResource) {
        IstioResource r = istioResource;
        ObjectMeta meta = r.getMetadata();
        client.deleteByName(meta.getName(), meta.getNamespace(), r.getKind());
    }

    @Override
    public void update(IstioResource istioResource) {
        client.createOrUpdate(istioResource);
    }

    @Override
    public IstioResource get(IstioResource istioResource) {
        IstioResource r = istioResource;
        ObjectMeta meta = r.getMetadata();
        return get(meta.getName(), meta.getNamespace(), r.getKind());
    }

    @Override
    public IstioResource get(String kind, String namespace, String name) {
        return (IstioResource) client.get(name, namespace, kind);
    }

    @Override
    public boolean exist(IstioResource t) {
        return get(t) != null;
    }
}
