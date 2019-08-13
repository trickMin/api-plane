package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netease.cloud.nsf.util.PluginConst;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/12
 **/
public class ServiceInfo {
    @JsonProperty(PluginConst.TEMPLATE_APINAME)
    private String apiName = String.format("${%s}", PluginConst.TEMPLATE_APINAME);
    @JsonProperty(PluginConst.TEMPLATE_METHOD)
    private String method = String.format("${%s}", PluginConst.TEMPLATE_METHOD);
    @JsonProperty(PluginConst.TEMPLATE_URI)
    private String uri = String.format("${%s}", PluginConst.TEMPLATE_URI);
    @JsonProperty(PluginConst.TEMPLATE_SUBSET)
    private String subset = String.format("${%s}", PluginConst.TEMPLATE_SUBSET);

    public String getApiName() {
        return apiName;
    }

    @JsonProperty(PluginConst.TEMPLATE_APINAME)
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getMethod() {
        return method;
    }

    @JsonProperty(PluginConst.TEMPLATE_METHOD)
    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    @JsonProperty(PluginConst.TEMPLATE_URI)
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getSubset() {
        return subset;
    }

    @JsonProperty(PluginConst.TEMPLATE_SUBSET)
    public void setSubset(String subset) {
        this.subset = subset;
    }
}
