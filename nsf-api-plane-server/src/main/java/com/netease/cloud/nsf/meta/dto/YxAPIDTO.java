package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netease.cloud.nsf.meta.ApiOption;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class YxAPIDTO {

    /**
     * 网关名
     */
    @NotEmpty(message = "gateways")
    @JsonProperty(value = "Gateways")
    private List<String> gateways;

    /**
     * api名
     */
    @NotEmpty(message = "api name")
    @JsonProperty(value = "Name")
    private String name;

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
    @NotEmpty(message = "proxy uris")
    @JsonProperty(value = "ProxyUris")
    private List<String> proxyUris;

    /**
     * 服务名
     */
    @NotEmpty(message = "service name")
    @JsonProperty(value = "Service")
    private String service;

    /**
     * 插件
     */
    @JsonProperty(value = "Plugins")
    private List<String> plugins;

    /**
     * 高级选项
     */
    @Valid
    @NotNull(message = "Option")
    @JsonProperty(value = "Option")
    private ApiOption option;

    /**
     * query string
     */
    @JsonProperty(value = "QueryParams")
    @Valid
    private List<PairMatchDTO> queryParams;

    /**
     * 请求头
     */
    @JsonProperty(value = "Headers")
    @Valid
    private List<PairMatchDTO> headers;

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

    public ApiOption getOption() {
        return option;
    }

    public void setOption(ApiOption option) {
        this.option = option;
    }

    public String getUriMatch() {
        return uriMatch;
    }

    public void setUriMatch(String uriMatch) {
        this.uriMatch = uriMatch;
    }

    public List<PairMatchDTO> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<PairMatchDTO> queryParams) {
        this.queryParams = queryParams;
    }

    public List<PairMatchDTO> getHeaders() {
        return headers;
    }

    public void setHeaders(List<PairMatchDTO> headers) {
        this.headers = headers;
    }


    public static final class YxAPIDTOBuilder {
        private List<String> gateways;
        private String name;
        private List<String> hosts;
        private List<String> requestUris;
        private String uriMatch;
        private List<String> methods;
        private List<String> proxyUris;
        private String service;
        private List<String> plugins;
        private ApiOption option;
        private List<PairMatchDTO> queryParams;
        private List<PairMatchDTO> headers;

        public YxAPIDTOBuilder() {
        }

        public static YxAPIDTOBuilder anYxAPIDTO() {
            return new YxAPIDTOBuilder();
        }

        public YxAPIDTOBuilder withGateways(List<String> gateways) {
            this.gateways = gateways;
            return this;
        }

        public YxAPIDTOBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public YxAPIDTOBuilder withHosts(List<String> hosts) {
            this.hosts = hosts;
            return this;
        }

        public YxAPIDTOBuilder withRequestUris(List<String> requestUris) {
            this.requestUris = requestUris;
            return this;
        }

        public YxAPIDTOBuilder withUriMatch(String uriMatch) {
            this.uriMatch = uriMatch;
            return this;
        }

        public YxAPIDTOBuilder withMethods(List<String> methods) {
            this.methods = methods;
            return this;
        }

        public YxAPIDTOBuilder withProxyUris(List<String> proxyUris) {
            this.proxyUris = proxyUris;
            return this;
        }

        public YxAPIDTOBuilder withService(String service) {
            this.service = service;
            return this;
        }

        public YxAPIDTOBuilder withPlugins(List<String> plugins) {
            this.plugins = plugins;
            return this;
        }

        public YxAPIDTOBuilder withOption(ApiOption option) {
            this.option = option;
            return this;
        }

        public YxAPIDTOBuilder withQueryParams(List<PairMatchDTO> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public YxAPIDTOBuilder withHeaders(List<PairMatchDTO> headers) {
            this.headers = headers;
            return this;
        }

        public YxAPIDTO build() {
            YxAPIDTO yxAPIDTO = new YxAPIDTO();
            yxAPIDTO.setGateways(gateways);
            yxAPIDTO.setName(name);
            yxAPIDTO.setHosts(hosts);
            yxAPIDTO.setRequestUris(requestUris);
            yxAPIDTO.setUriMatch(uriMatch);
            yxAPIDTO.setMethods(methods);
            yxAPIDTO.setProxyUris(proxyUris);
            yxAPIDTO.setService(service);
            yxAPIDTO.setPlugins(plugins);
            yxAPIDTO.setOption(option);
            yxAPIDTO.setQueryParams(queryParams);
            yxAPIDTO.setHeaders(headers);
            return yxAPIDTO;
        }
    }
}
