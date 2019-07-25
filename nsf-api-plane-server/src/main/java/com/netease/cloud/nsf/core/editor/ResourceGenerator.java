package com.netease.cloud.nsf.core.editor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import com.netease.cloud.nsf.exception.ApiPlaneException;

import java.io.IOException;
import java.util.Objects;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class ResourceGenerator implements Editor {

    private EditorContext editorContext;
    private String originalJson;
    private DocumentContext jsonContext;

    private ResourceGenerator(Object resource, ResourceType type, EditorContext editorContext) {
        if (Objects.isNull(resource) || Objects.isNull(type) || Objects.isNull(editorContext)) {
            throw new ApiPlaneException("ResourceGenerator's construction parameter cannot be empty");
        }
        this.editorContext = editorContext;
        switch (type) {
            case JSON:
                this.originalJson = (String) resource;
                break;
            case YAML:
                this.originalJson = yaml2json((String) resource);
                break;
            case OBJECT:
                this.originalJson = obj2json(resource);
                break;
        }
        this.jsonContext = JsonPath.using(editorContext.configuration()).parse(originalJson);
    }

    public static ResourceGenerator newInstance(Object resource, ResourceType type, EditorContext editorContext) {
        return new ResourceGenerator(resource, type, editorContext);
    }

    @Override
    public <T> T getValue(String path, Predicate... filter) {
        return jsonContext.read(path, filter);
    }

    @Override
    public <T> T getValue(String path, Class<T> type, Predicate... filter) {
        return jsonContext.read(path, type, filter);
    }

    @Override
    public synchronized void addElement(String path, Object value) {
        jsonContext.add(path, value);
    }


    @Override
    public synchronized void removeElement(String path) {
        jsonContext.delete(path);
    }

    @Override
    public void updateValue(String path, Object value) {
        jsonContext.set(path, value);
    }

    @Override
    public void createOrUpdateValue(String path, String key, Object value) {
        jsonContext.put(path, key, value);
    }

    @Override
    public synchronized String jsonString() {
        return jsonContext.jsonString();
    }

    @Override
    public synchronized String yamlString() {
        return json2yaml(jsonContext.jsonString());
    }


    private String yaml2json(String yaml) {
        try {
            Object obj = editorContext.yamlMapper().readValue(yaml, Object.class);
            return editorContext.jsonMapper().writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApiPlaneException("convert yaml to json failed.", e);
        }

    }

    private String json2yaml(String json) {
        try {
            Object obj = editorContext.jsonMapper().readValue(json, Object.class);
            return editorContext.yamlMapper().writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApiPlaneException("convert json to yaml failed.", e);
        }
    }

    private String obj2json(Object obj) {
        try {
            return editorContext.jsonMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ApiPlaneException("convert obj to json failed.", e);
        }
    }
}
