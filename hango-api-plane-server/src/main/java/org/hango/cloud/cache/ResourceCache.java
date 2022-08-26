package org.hango.cloud.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

/**
* @Author: zhufengwei.sx
* @Date: 2022/8/26 14:37
**/
public interface ResourceCache {

    /**
     * 获取资源类型
     * @param kind 资源类型
     * @return 资源列表
     */
    List<HasMetadata> getResource(String kind);

    /**
     * 刷新资源
     * @param kind 资源类型
     * @param names 资源名称
     */
    void refresh(String kind, List<String> names);

}
