package com.netease.cloud.nsf.core.k8s.http;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/31
 **/
public interface K8sHttpClient {

    String getWithNull(String kind, String namespace, String name);

    String get(String kind, String namespace, String name);

    String put(String resource);

    String post(String resource);

    String delete(String kind, String namespace, String name);
}
