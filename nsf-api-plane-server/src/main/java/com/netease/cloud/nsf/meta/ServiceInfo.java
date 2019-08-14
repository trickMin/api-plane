package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;
/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/12
 **/
public class ServiceInfo {
    @JsonProperty(VIRTUAL_SERVICE_NAME)
    private String apiName = String.format("${%s}", VIRTUAL_SERVICE_NAME);
    @JsonProperty(API_METHODS)
    private String method = String.format("${%s}", API_METHODS);
    @JsonProperty(VIRTUAL_SERVICE_HOSTS)
    private String uri = String.format("${%s}", VIRTUAL_SERVICE_HOSTS);
    @JsonProperty(VIRTUAL_SERVICE_SUBSET_NAME)
    private String subset = String.format("${%s}", VIRTUAL_SERVICE_SUBSET_NAME);
    @JsonProperty(VIRTUAL_SERVICE_DESTINATIONS)
    private String destinations = String.format("${%s}", VIRTUAL_SERVICE_DESTINATIONS);

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

    public String getDestinations() {
        return destinations;
    }

    @JsonProperty(VIRTUAL_SERVICE_DESTINATIONS)
    public void setDestinations(String destinations) {
        this.destinations = destinations;
    }
}
