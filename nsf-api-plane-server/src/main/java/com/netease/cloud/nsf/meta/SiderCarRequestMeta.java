package com.netease.cloud.nsf.meta;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/29
 **/
public class SiderCarRequestMeta {
    private String by;
    private String hash;
    private String subject;
    private String uri;

    // 以下三个从uri中解析得到
    private String cluster;
    private String service;
    private String namespace;

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
