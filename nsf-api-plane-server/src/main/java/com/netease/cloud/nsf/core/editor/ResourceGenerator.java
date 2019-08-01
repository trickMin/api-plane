package com.netease.cloud.nsf.core.editor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;

import java.io.IOException;
import java.util.Objects;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public class ResourceGenerator implements Editor {

    protected EditorContext editorContext;
    protected String originalJson;
    protected DocumentContext jsonContext;

    protected ResourceGenerator(Object resource, ResourceType type, EditorContext editorContext) {
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
    public boolean contain(String path, Predicate... filter) {
        return jsonContext.read(path, filter) != null;
    }

    @Override
    public <T> T getValue(String path, Predicate... filter) {
        return (T) jsonContext.read(path, filter);
    }

    @Override
    public <T> T getValue(String path, Class<T> type, Predicate... filter) {
        return jsonContext.read(path, type, filter);
    }

    @Override
    public synchronized void addElement(String path, Object value, Predicate... filter) {
        jsonContext.add(path, value, filter);
    }


    @Override
    public synchronized void removeElement(String path, Predicate... filter) {
        jsonContext.delete(path, filter);
    }

    @Override
    public void updateValue(String path, Object value, Predicate... filter) {
        jsonContext.set(path, value, filter);
    }

    @Override
    public void createOrUpdateValue(String path, String key, Object value, Predicate... filter) {
        jsonContext.put(path, key, value, filter);
    }

    @Override
    public synchronized String jsonString() {
        return jsonContext.jsonString();
    }

    @Override
    public synchronized String yamlString() {
        return json2yaml(jsonContext.jsonString());
    }

    @Override
    public <T> T object(Class<T> type) {
        return jsonContext.read("$", type);
    }


    protected String yaml2json(String yaml) {
        try {
            Object obj = editorContext.yamlMapper().readValue(yaml, Object.class);
            return editorContext.jsonMapper().writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApiPlaneException("convert yaml to json failed.", e);
        }

    }

    protected String json2yaml(String json) {
        try {
            Object obj = editorContext.jsonMapper().readValue(json, Object.class);
            return editorContext.yamlMapper().writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ApiPlaneException("convert json to yaml failed.", e);
        }
    }

    protected String obj2json(Object obj) {
        try {
            return editorContext.jsonMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ApiPlaneException("convert obj to json failed.", e);
        }
    }
}
