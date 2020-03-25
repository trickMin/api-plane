package com.netease.cloud.nsf.core.k8s.empty;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/3/19
 **/
public class EmptyConfigMap extends ConfigMap implements HasMetadata, EmptyResource {

    private String kind = "ConfigMap";
    private ObjectMeta om;

    public EmptyConfigMap(String name) {
        ObjectMeta tom = new ObjectMeta();
        tom.setName(name);
        this.om = tom;
    }

    @Override
    public ObjectMeta getMetadata() {
        return om;
    }

    @Override
    public void setMetadata(ObjectMeta objectMeta) {

    }

    @Override
    public String getKind() {
        return kind;
    }

    @Override
    public String getApiVersion() {
        return null;
    }

    @Override
    public void setApiVersion(String s) {

    }
}
