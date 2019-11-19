package com.netease.cloud.nsf.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;

/**
 * @author zhangzihao
 */
public interface ResourceFilter<T extends HasMetadata> {

    /**
     * 再处理资源前对指定类型的资源进行过滤
     * @param obj 资源对象
     * @return 匹配结果
     */
    boolean match(T obj);

}
