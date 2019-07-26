package com.netease.cloud.nsf.core.editor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;


/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class EditorContext {
    private ObjectMapper jsonMapper;

    private ObjectMapper yamlMapper;

    private Configuration configuration;

    public ObjectMapper jsonMapper() {
        return jsonMapper;
    }

    public ObjectMapper yamlMapper() {
        return yamlMapper;
    }

    public Configuration configuration() {
        return configuration;
    }

    public EditorContext(ObjectMapper jsonMapper, ObjectMapper yamlMapper, Configuration configuration) {
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
        this.configuration = configuration;
    }
}
