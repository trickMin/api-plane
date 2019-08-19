package com.netease.cloud.nsf.core.editor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
                this.originalJson = yaml2json((String) resource, editorContext);
                break;
            case OBJECT:
                this.originalJson = obj2json(resource, editorContext);
                break;
        }
        this.jsonContext = JsonPath.using(editorContext.configuration()).parse(originalJson);
    }

    public static ResourceGenerator newInstance(Object resource, ResourceType type, EditorContext editorContext) {
        return new ResourceGenerator(resource, type, editorContext);
    }

    @Override
    public boolean contain(String path, Predicate... filter) {
        Object result = jsonContext.read(path, filter);
        if (result instanceof List) {
            return ((List) result).size() != 0;
        }
        if (result instanceof Map) {
            return ((Map) result).size() != 0;
        }
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
    public void addJsonElement(String path, String json, Predicate... filter) {
        if (json.startsWith("[")) {
            addElement(path, json2obj(json, List.class, editorContext), filter);
        } else if (json.startsWith("{")) {
            addElement(path, json2obj(json, Map.class, editorContext), filter);
        } else {
            createOrUpdateValue(path, json, filter);
        }
    }

    @Override
    public void createOrUpdateJson(String path, String key, String json, Predicate... filter) {
        if (json.startsWith("[")) {
            createOrUpdateValue(path, key, json2obj(json, List.class, editorContext), filter);
        } else if (json.startsWith("{")) {
            createOrUpdateValue(path, key, json2obj(json, Map.class, editorContext), filter);
        } else {
            createOrUpdateValue(path, key, json, filter);
        }
    }

    @Override
    public synchronized String jsonString() {
        return jsonContext.jsonString();
    }

    @Override
    public synchronized String yamlString() {
        return json2yaml(jsonContext.jsonString(), editorContext);
    }

    @Override
    public <T> T object(Class<T> type) {
        return jsonContext.read("$", type);
    }


    public static String yaml2json(String yaml, EditorContext editorContext) {
        try {
            Object obj = editorContext.yamlMapper().readValue(yaml, Object.class);
            return editorContext.jsonMapper().writeValueAsString(obj);
        } catch (IOException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }

    }

    public static String json2yaml(String json, EditorContext editorContext) {
        try {
            Object obj = editorContext.jsonMapper().readValue(json, Object.class);
            return editorContext.yamlMapper().writeValueAsString(obj);
        } catch (IOException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    public static String obj2json(Object obj, EditorContext editorContext) {
        try {
            return editorContext.jsonMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    public static <T> T json2obj(String json, Class<T> type, EditorContext editorContext) {
        try {
            return editorContext.jsonMapper().readValue(json, type);
        } catch (IOException e) {
            throw new ApiPlaneException(e.getMessage(), e);
        }
    }

    public static <T> T yaml2obj(String yaml, Class<T> type, EditorContext editorContext) {
        return json2obj(yaml2json(yaml, editorContext), type, editorContext);
    }
}
