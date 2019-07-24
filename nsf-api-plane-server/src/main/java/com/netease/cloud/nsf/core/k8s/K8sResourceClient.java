package com.netease.cloud.nsf.core.k8s;


import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
public interface K8sResourceClient<T extends HasMetadata> {

    void createOrUpdate(List<T> resources);

    void createOrUpdate(T resource);

    void delete(String kind, String name, String namespace);

    List<T> getList(String kind, String namespace);

    T get(String kind, String name, String namespace);

    boolean isAdapt(String kind);
}
