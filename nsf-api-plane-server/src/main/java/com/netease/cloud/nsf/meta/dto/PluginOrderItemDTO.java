package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.snowdrop.istio.api.Struct;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/11/21
 **/
public class PluginOrderItemDTO {
    @JsonProperty("enable")
    private boolean enable;
    @JsonProperty("name")
    private String name;
    @JsonProperty("settings")
    private Object settings;

    public boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getSettings() {
        return settings;
    }

    public void setSettings(Object settings) {
        this.settings = settings;
    }
}
