package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/12
 **/
public class ServiceInfo {
    @JsonIgnore
    private API api;

    @JsonProperty(VIRTUAL_SERVICE_NAME)
    private String apiName;
    @JsonProperty(API_METHODS)
    private String method;
    @JsonProperty(VIRTUAL_SERVICE_HOSTS)
    private String uri;
    @JsonProperty(VIRTUAL_SERVICE_SUBSET_NAME)
    private String subset;
    @JsonProperty(VIRTUAL_SERVICE_ROUTE)
    private String route;
    @JsonProperty(VIRTUAL_SERVICE_MATCH)
    private String match;
    @JsonProperty(VIRTUAL_SERVICE_EXTRA)
    private String exact;


    public API getApi() {
        return api;
    }

    @JsonIgnore
    public void setApi(API api) {
        this.api = api;
    }

    public String getApiName() {
        return apiName;
    }

    @JsonProperty(VIRTUAL_SERVICE_NAME)
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getMethod() {
        return method;
    }

    @JsonProperty(API_METHODS)
    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    @JsonProperty(VIRTUAL_SERVICE_HOSTS)
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSubset() {
        return subset;
    }

    @JsonProperty(VIRTUAL_SERVICE_SUBSET_NAME)
    public void setSubset(String subset) {
        this.subset = subset;
    }

    public String getRoute() {
        return route;
    }

    @JsonProperty(VIRTUAL_SERVICE_ROUTE)
    public void setRoute(String route) {
        this.route = route;
    }

    public String getMatch() {
        return match;
    }

    @JsonProperty(VIRTUAL_SERVICE_MATCH)
    public void setMatch(String match) {
        this.match = match;
    }

    public String getExact() {
        return exact;
    }

    @JsonProperty(VIRTUAL_SERVICE_EXTRA)
    public void setExact(String exact) {
        this.exact = exact;
    }
}
