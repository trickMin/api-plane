package com.netease.cloud.nsf.core.editor;

import com.jayway.jsonpath.Predicate;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
public interface Editor {
    boolean contain(String path, Predicate... filter);

    <T> T getValue(String path, Predicate... filter);

    <T> T getValue(String path, Class<T> type, Predicate... filter);

    void addElement(String path, Object value, Predicate... filter);

    void removeElement(String path, Predicate... filter);

    void updateValue(String path, Object value, Predicate... filter);

    void createOrUpdateValue(String path, String key, Object value, Predicate... filter);

    /**
     * 导出为json
     *
     * @return
     */
    String jsonString();

    /**
     * 导出为yaml
     *
     * @return
     */
    String yamlString();

    /**
     * 导出为object
     *
     * @param type
     * @param <T>
     * @return
     */
    <T> T object(Class<T> type);
}
