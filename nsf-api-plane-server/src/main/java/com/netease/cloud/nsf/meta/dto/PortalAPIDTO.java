package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/19
 **/
public class PortalAPIDTO {

    @NotEmpty(message = "gateway")
    @JsonProperty(value = "Gateway")
    private String gateway;

    /**
     * api唯一标识
     */
    @NotEmpty(message = "api code")
    @JsonProperty(value = "Code")
    private String code;

    @NotEmpty(message = "hosts")
    @JsonProperty(value = "Hosts")
    private List<String> hosts;

    /**
     * 请求uri
     */
    @NotEmpty(message = "request uris")
    @JsonProperty(value = "RequestUris")
    private List<String> requestUris;

    /**
     * 请求uri匹配方式
     */
    @NotEmpty(message = "uri match")
    @JsonProperty(value = "UriMatch")
    private String uriMatch;

    /**
     * 请求方法,GET、POST...
     */
    @NotEmpty(message = "http methods")
    @JsonProperty(value = "Methods")
    private List<String> methods;

    /**
     * 映射到后端的uri
     */
    @Valid
    @JsonProperty(value = "ProxyServices")
    @NotNull(message = "proxyServices")
    private List<PortalRouteServiceDTO> proxyServices;

    /**
     * 插件
     */
    @JsonProperty(value = "Plugins")
    private List<String> plugins;

    /**
     * 请求头
     */
    @JsonProperty(value = "Headers")
    @Valid
    private List<PairMatchDTO> headers;

    @JsonProperty(value = "Order")
    private Integer priority;
    /**
     * 请求的query params
     */
    @JsonProperty(value = "QueryParams")
    @Valid
    private List<PairMatchDTO> queryParams;

    /**
     * 服务标志
     */
    @JsonProperty(value = "ServiceTag")
    @NotNull(message = "ServiceTag")
    private String serviceTag;

    /**
     * 路由id
     */
    @JsonProperty(value = "RouteId")
    @NotNull(message = "RouteId")
    private Long routeId;

    /**
     * 路由名字
     */
    @JsonProperty(value = "RouteName")
    @NotNull(message = "RouteName")
    private String routeName;

    /**
     * 路由超时时间
     */
    @JsonProperty(value = "Timeout")
    private Long timeout;

    /**
     * 路由重试策略
     */
    @JsonProperty(value = "HttpRetry")
    private HttpRetryDto httpRetry;

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<String> getRequestUris() {
        return requestUris;
    }

    public void setRequestUris(List<String> requestUris) {
        this.requestUris = requestUris;
    }

    public String getUriMatch() {
        return uriMatch;
    }

    public void setUriMatch(String uriMatch) {
        this.uriMatch = uriMatch;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public List<PortalRouteServiceDTO> getProxyServices() {
        return proxyServices;
    }

    public void setProxyServices(List<PortalRouteServiceDTO> proxyServices) {
        this.proxyServices = proxyServices;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public List<PairMatchDTO> getHeaders() {
        return headers;
    }

    public void setHeaders(List<PairMatchDTO> headers) {
        this.headers = headers;
    }

    public List<PairMatchDTO> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<PairMatchDTO> queryParams) {
        this.queryParams = queryParams;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public HttpRetryDto getHttpRetry() {
        return httpRetry;
    }

    public void setHttpRetry(HttpRetryDto httpRetry) {
        this.httpRetry = httpRetry;
    }
}
