package com.netease.cloud.nsf.meta.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netease.cloud.nsf.meta.ApiOption;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class YxAPI {

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

    @JsonProperty(value = "Hosts")
    private List<String> hosts;

    /**
     * 请求uri
     */
    @JsonProperty(value = "RequestUris")
    private List<String> requestUris;

    /**
     * 请求uri匹配方式
     */
    @JsonProperty(value = "UriMatch")
    private String uriMatch;

    /**
     * 请求方法,GET、POST...
     */
    @JsonProperty(value = "Methods")
    private List<String> methods;

    /**
     * 映射到后端的uri
     */
    @JsonProperty(value = "ProxyUris")
    private List<String> proxyUris;

    /**
     * 服务名
     */
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


    public static final class YxAPIModelBuilder {
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

        private YxAPIModelBuilder() {
        }

        public static YxAPIModelBuilder anYxAPIModel() {
            return new YxAPIModelBuilder();
        }

        public YxAPIModelBuilder withGateways(List<String> gateways) {
            this.gateways = gateways;
            return this;
        }

        public YxAPIModelBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public YxAPIModelBuilder withHosts(List<String> hosts) {
            this.hosts = hosts;
            return this;
        }

        public YxAPIModelBuilder withRequestUris(List<String> requestUris) {
            this.requestUris = requestUris;
            return this;
        }

        public YxAPIModelBuilder withUriMatch(String uriMatch) {
            this.uriMatch = uriMatch;
            return this;
        }

        public YxAPIModelBuilder withMethods(List<String> methods) {
            this.methods = methods;
            return this;
        }

        public YxAPIModelBuilder withProxyUris(List<String> proxyUris) {
            this.proxyUris = proxyUris;
            return this;
        }

        public YxAPIModelBuilder withService(String service) {
            this.service = service;
            return this;
        }

        public YxAPIModelBuilder withPlugins(List<String> plugins) {
            this.plugins = plugins;
            return this;
        }

        public YxAPIModelBuilder withOption(ApiOption option) {
            this.option = option;
            return this;
        }

        public YxAPI build() {
            YxAPI yxAPIModel = new YxAPI();
            yxAPIModel.setGateways(gateways);
            yxAPIModel.setName(name);
            yxAPIModel.setHosts(hosts);
            yxAPIModel.setRequestUris(requestUris);
            yxAPIModel.setUriMatch(uriMatch);
            yxAPIModel.setMethods(methods);
            yxAPIModel.setProxyUris(proxyUris);
            yxAPIModel.setService(service);
            yxAPIModel.setPlugins(plugins);
            yxAPIModel.setOption(option);
            return yxAPIModel;
        }
    }
}
