package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public class Plugin {

    private String name;

    private String processor;

    private String description;

    private String author;

    private String createTime;

    private String updateTime;

    private String pluginScope;

    private String pluginPriority;

    private String instructionForUse;

    @JsonIgnore
    private String schema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getPluginScope() {
        return pluginScope;
    }

    public void setPluginScope(String pluginScope) {
        this.pluginScope = pluginScope;
    }

    public String getPluginPriority() {
        return pluginPriority;
    }

    public void setPluginPriority(String pluginPriority) {
        this.pluginPriority = pluginPriority;
    }

    public String getInstructionForUse() {
        return instructionForUse;
    }

    public void setInstructionForUse(String instructionForUse) {
        this.instructionForUse = instructionForUse;
    }

    @Override
    public String toString() {
        return "Plugin{" +
                "name='" + name + '\'' +
                ", processor='" + processor + '\'' +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", pluginScope='" + pluginScope + '\'' +
                ", pluginPriority='" + pluginPriority + '\'' +
                ", instructionForUse='" + instructionForUse + '\'' +
                ", schema='" + schema + '\'' +
                '}';
    }
}
