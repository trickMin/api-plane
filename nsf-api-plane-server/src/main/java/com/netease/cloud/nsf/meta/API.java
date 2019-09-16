package com.netease.cloud.nsf.meta;


import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/2
 **/
public class API {

    private List<String> gateways;

    /**
     * api名
     */
    private String name;

    private List<String> hosts;

    /**
     * 请求uri
     */
    private List<String> requestUris;

    /**
     * 请求uri匹配方式, REGEX,PREFIX,EXACT
     */
    private UriMatch uriMatch;

    /**
     * 请求方法,GET、POST...
     */
    private List<String> methods;

    /**
     * 映射到后端的uri
     */
    private List<String> proxyUris;

    /**
     * 服务名
     */
    private String service;

    /**
     * 插件
     */
    private List<String> plugins;

    private Boolean extractMethod;
    /**
     * 负载均衡
     */
    private String loadBalancer;

    /**
     * 请求是否幂等
     */
    private Boolean Idempotent;

    /**
     * 保留原始host
     */
    private Boolean preserveHost = true;

    /**
     * 重试次数
     */
    private Integer retries = 5;

    private Long connectTimeout;

    /**
     * 上下游超时时间(发送、读取)
     */
    private Long IdleTimeout;

    private Boolean httpsOnly;

    private Boolean httpIfTerminated;

    /**
     * 协议，默认HTTP
     */
    private String protocol = "HTTP";

    /**
     * 网关暴露端口，默认80
     */
    private Integer port = 80;

    public List<String> getGateways() {
        return gateways;
    }

    public void setGateways(List<String> gateways) {
        this.gateways = gateways;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public List<String> getProxyUris() {
        return proxyUris;
    }

    public void setProxyUris(List<String> proxyUris) {
        this.proxyUris = proxyUris;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<String> plugins) {
        this.plugins = plugins;
    }

    public Boolean getExtractMethod() {
        return extractMethod;
    }

    public void setExtractMethod(Boolean extractMethod) {
        this.extractMethod = extractMethod;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public Boolean getIdempotent() {
        return Idempotent;
    }

    public void setIdempotent(Boolean idempotent) {
        Idempotent = idempotent;
    }

    public Boolean getPreserveHost() {
        return preserveHost;
    }

    public void setPreserveHost(Boolean preserveHost) {
        this.preserveHost = preserveHost;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Long getIdleTimeout() {
        return IdleTimeout;
    }

    public void setIdleTimeout(Long idleTimeout) {
        IdleTimeout = idleTimeout;
    }

    public Boolean getHttpsOnly() {
        return httpsOnly;
    }

    public void setHttpsOnly(Boolean httpsOnly) {
        this.httpsOnly = httpsOnly;
    }

    public Boolean getHttpIfTerminated() {
        return httpIfTerminated;
    }

    public void setHttpIfTerminated(Boolean httpIfTerminated) {
        this.httpIfTerminated = httpIfTerminated;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public UriMatch getUriMatch() {
        return uriMatch;
    }

    public void setUriMatch(UriMatch uriMatch) {
        this.uriMatch = uriMatch;
    }
}
