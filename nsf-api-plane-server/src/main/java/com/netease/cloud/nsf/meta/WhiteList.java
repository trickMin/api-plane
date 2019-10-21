package com.netease.cloud.nsf.meta;

import com.sun.javafx.binding.StringFormatter;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class WhiteList {

    private String service;
    private String namespace;

    public void setService(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }

    public String getFullService() {
        return StringFormatter.format("%s.%s.svc.cluster.local", service, namespace).getValue();
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSourcesNamespace() {
        return getNamespace();
    }

}
