package com.netease.cloud.nsf.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/11/21
 **/
public class PluginOrderItemDTO {

    @JsonProperty("Enable")
    @NotNull(message = "enable")
    private Boolean enable;

    @JsonProperty("Name")
    @NotNull(message = "name")
    private String name;

    @JsonProperty("Settings")
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
