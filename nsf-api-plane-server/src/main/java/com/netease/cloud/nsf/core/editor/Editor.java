package com.netease.cloud.nsf.core.editor;

import com.jayway.jsonpath.Predicate;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public interface Editor {
    <T> T getValue(String path, Predicate... filter);

    <T> T getValue(String path, Class<T> type, Predicate... filter);

    void addElement(String path, Object value);

    void removeElement(String path);

    void updateValue(String path, Object value);

    void createOrUpdateValue(String path, String key, Object value);

    String jsonString();

    String yamlString();
}
