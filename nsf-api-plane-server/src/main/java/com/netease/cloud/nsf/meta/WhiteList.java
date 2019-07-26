package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netease.cloud.nsf.validator.annotation.ConditionalTemplate;

import java.util.List;
import java.util.Map;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class WhiteList {
    @JsonProperty("name")
    private String name;

    @JsonProperty("namespace")
    private String namespace;

    @JsonProperty("users")
    private List<String> users;

    @JsonProperty("service")
    private String service;

    @JsonProperty("values")
    private List<String> values;

    @JsonProperty("header")
    private String header;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
