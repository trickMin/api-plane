package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/8/12
 **/
public class ServiceInfo {
    // API中有和API相关的所有信息
    @JsonIgnore
    private API api;

     // 提供apiName占位符，例如${t_api_name},后续GatewayModelProcessor会进行渲染
     // 也可直接从API中获得apiName
    @JsonProperty(VIRTUAL_SERVICE_NAME)
    private String apiName;

    // 提供method占位符，例如${t_api_methods},后续GatewayModelProcessor会进行渲染
    // 也可直接从API中获得method
    @JsonProperty(API_METHODS)
    private String method;

    // 提供uri占位符，例如${t_api_request_uris},后续GatewayModelProcessor会进行渲染
    // 也可直接从API中获得uri
    @JsonProperty(VIRTUAL_SERVICE_HOSTS)
    private String uri;

    // 提供subset占位符，例如${t_virtual_service_subset_name},后续GatewayModelProcessor会进行渲染，不能从API中获得
    @JsonProperty(VIRTUAL_SERVICE_SUBSET_NAME)
    private String subset;

    /**
     * 提供渲染好的默认destination
     * - destination:
     *     host: productpage.default.svc.cluster.local
     *     port:
     *       number: 9080
     *     subset: service-zero-plane-istio-test-gateway-yx
     *   weight: 100
     */
    @JsonProperty(VIRTUAL_SERVICE_ROUTE)
    private String route;

    /**
     * 提供渲染好的默认match
     * match:
     * - uri:
     *     regex: .*
     *   method:
     *     regex: GET|POST
     */
    @JsonProperty(VIRTUAL_SERVICE_MATCH)
    private String match;

    /**
     * 提供额外渲染好的的资源片段
     */
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
