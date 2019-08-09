package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class Endpoint {

    @JsonProperty(value = "Name")
    private String hostname;

    @JsonProperty(value = "Address")
    private String address;

    @JsonProperty(value = "Port")
    private Integer port;

    @JsonProperty(value = "Labels")
    private Map<String,String> labels;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Endpoint endpoint = (Endpoint) o;
        return Objects.equals(hostname, endpoint.hostname) &&
                Objects.equals(address, endpoint.address) &&
                Objects.equals(port, endpoint.port) &&
                Objects.equals(labels, endpoint.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, address, port, labels);
    }
}