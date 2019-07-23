package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class APIModel {

    /**
     * 网关名
     */
    @NotEmpty(message = "gateways")
    @JsonProperty(value = "Gateways")
    private List<String> gateways;

    /**
     * api名
     */
    @NotEmpty
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


    public static final class APIModelBuilder {
        private List<String> gateways;
        private String name;
        private List<String> hosts;
        private List<String> requestUris;
        private List<String> methods;
        private List<String> proxyUris;
        private String service;
        private List<String> plugins;
        private ApiOption option;

        private APIModelBuilder() {
        }

        public static APIModelBuilder anAPIModel() {
            return new APIModelBuilder();
        }

        public APIModelBuilder withGateways(List<String> gateways) {
            this.gateways = gateways;
            return this;
        }

        public APIModelBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public APIModelBuilder withHosts(List<String> hosts) {
            this.hosts = hosts;
            return this;
        }

        public APIModelBuilder withRequestUris(List<String> requestUris) {
            this.requestUris = requestUris;
            return this;
        }

        public APIModelBuilder withMethods(List<String> methods) {
            this.methods = methods;
            return this;
        }

        public APIModelBuilder withProxyUris(List<String> proxyUris) {
            this.proxyUris = proxyUris;
            return this;
        }

        public APIModelBuilder withService(String service) {
            this.service = service;
            return this;
        }

        public APIModelBuilder withPlugins(List<String> plugins) {
            this.plugins = plugins;
            return this;
        }

        public APIModelBuilder withOption(ApiOption option) {
            this.option = option;
            return this;
        }

        public APIModel build() {
            APIModel aPIModel = new APIModel();
            aPIModel.setGateways(gateways);
            aPIModel.setName(name);
            aPIModel.setHosts(hosts);
            aPIModel.setRequestUris(requestUris);
            aPIModel.setMethods(methods);
            aPIModel.setProxyUris(proxyUris);
            aPIModel.setService(service);
            aPIModel.setPlugins(plugins);
            aPIModel.setOption(option);
            return aPIModel;
        }
    }
}
