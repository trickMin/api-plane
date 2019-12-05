package com.netease.cloud.nsf.cache;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ListMeta;

import java.util.List;

/**
 * @author zhangzihao
 */
public class ClusterResourceList implements KubernetesResourceList {

    private KubernetesResourceList delegate;

    private String cluster;

    public ClusterResourceList(KubernetesResourceList delegate, String cluster) {
        this.delegate = delegate;
        this.cluster = cluster;
    }

    @Override
    public ListMeta getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public List getItems() {
        return delegate.getItems();
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
}
