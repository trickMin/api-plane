package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/12
 **/
public class ServiceInfo {
    // 提供apiName占位符，例如${t_api_name},后续GatewayModelProcessor会进行渲染
    // 也可直接从API中获得apiName
    @JsonProperty(API_NAME)
    private String apiName;

    @JsonProperty(API_SERVICE)
    private String serviceName;

    // 提供method占位符，例如${t_api_methods},后续GatewayModelProcessor会进行渲染
    // 也可直接从API中获得method
    @JsonProperty(API_METHODS)
    private String method;

    // 提供uri占位符，例如${t_api_request_uris},后续GatewayModelProcessor会进行渲染
    // 也可直接从API中获得uri
    @JsonProperty(VIRTUAL_SERVICE_HOSTS_YAML)
    private String uri;

    // 提供subset占位符，例如${t_virtual_service_subset_name},后续GatewayModelProcessor会进行渲染，不能从API中获得
    @JsonProperty(VIRTUAL_SERVICE_SUBSET_NAME)
    private String subset;

    @JsonProperty(VIRTUAL_SERVICE_HOSTS)
    private String hosts;

    @JsonProperty(VIRTUAL_SERVICE_PLUGIN_MATCH_PRIORITY)
    private String priority;

    public String getApiName() {
        return apiName;
    }

    @JsonProperty(API_NAME)
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

    @JsonProperty(VIRTUAL_SERVICE_HOSTS_YAML)
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

    public String getHosts() {
        return hosts;
    }

    @JsonProperty(VIRTUAL_SERVICE_HOSTS)
    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getPriority() {
        return priority;
    }

    @JsonProperty(VIRTUAL_SERVICE_PLUGIN_MATCH_PRIORITY)
    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getServiceName() {
        return serviceName;
    }

    @JsonProperty(API_SERVICE)
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "apiName='" + apiName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", subset='" + subset + '\'' +
                ", hosts='" + hosts + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }
}
