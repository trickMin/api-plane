package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public class Plugin {

    private String name;

    private String processor;

    @JsonIgnore
    private String schema;

    private String description;

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

    @Override
    public String toString() {
        return "Plugin{" +
                "name='" + name + '\'' +
                ", processor=" + processor +
                '}';
    }
}
