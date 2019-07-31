package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.javafx.binding.StringFormatter;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class WhiteList {

    private SiderCarRequestMeta siderCarMeta;

    @JsonProperty("sources")
    private List<String> sources;

    @JsonProperty("outWeight")
    private int outWeight;

    @JsonProperty("authPaths")
    private List<String> authPaths;

    @JsonProperty("allPaths")
    private List<String> allPaths;

    public void setSiderCarMeta(SiderCarRequestMeta siderCarMeta) {
        this.siderCarMeta = siderCarMeta;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public int getOutWeight() {
        return outWeight;
    }

    public void setOutWeight(int outWeight) {
        this.outWeight = outWeight;
    }

    public String getService() {
        return siderCarMeta.getService();
    }

    public String getFullService() {
        return StringFormatter.format("%s.%s.svc.%s", siderCarMeta.getService(), siderCarMeta.getNamespace(), siderCarMeta.getCluster()).getValue();
    }

    public List<String> getAuthPaths() {
        return authPaths;
    }

    public void setAuthPaths(List<String> authPaths) {
        this.authPaths = authPaths;
    }

    public List<String> getAllPaths() {
        return allPaths;
    }

    public void setAllPaths(List<String> allPaths) {
        this.allPaths = allPaths;
    }

    public String getNamespace() {
        return siderCarMeta.getNamespace();
    }

    public String getSourcesNamespace() {
        return "yx-demo";
    }
}
