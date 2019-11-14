package com.netease.cloud.nsf.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;
import java.util.Map;


/**
 * @author zhangzihao
 */
public interface Store<T extends HasMetadata> {


    /**
     * 查询指定的资源
     * @param kind 资源类型
     * @param namespace 命名空间
     * @param name 资源名称
     * @return 资源对象
     */
    T get(String kind, String namespace, String name);

    /**
     * 添加资源
     * @param kind 资源类型
     * @param namespace 命名空间
     * @param name 资源名称
     * @param obj 资源对象
     */
    void add(String kind, String namespace, String name, T obj);

    /**
     * 更新资源
     * @param kind 资源类型
     * @param namespace 命名空间
     * @param name 资源名称
     * @param obj 资源对象
     */
    void update(String kind, String namespace, String name, T obj);

    /**
     * 清除资源
     * @param kind 资源类型
     * @param namespace 命名空间
     * @param name 资源名称
     * @return 被删除的对象
     */
    T delete(String kind, String namespace, String name);


    /**
     * @return 获取全部资源
     */
    List<T> list();

    /**
     * 查询指定类型资源
     * @param kind 资源类型
     * @return 指定类型的资源列表
     */
    List<T> listByKind(String kind);

    /**
     * @param kind 资源类型
     * @param namespace 命名空间
     * @return 指定类型指定命名空间资源
     */
    List<T> listByKindAndNamespace(String kind, String namespace);

    /**
     * 全量更新指定类型的资源
     * @param resourceMap 最新获取的资源信息
     * @param kind 资源类型
     */
    void replaceByKind(Map<String, Map<String, T>> resourceMap, String kind);

    /**
     * 查询指定命名空间的资源
     * @param namespace 命名空间
     * @return 资源列表
     */
    List<T> listByNamespace(String namespace);
}
