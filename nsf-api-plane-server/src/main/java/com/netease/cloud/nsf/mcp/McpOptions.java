package com.netease.cloud.nsf.mcp;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/17
 **/
public class McpOptions {
    // 注册支持的collection类型
    private Set<String> collections = new HashSet<>();
    // 注册descriptor，用于初始化序列化工具
    private Set<Descriptors.Descriptor> descriptors = new HashSet<>();
    // status检查间隔
    private long statusCheckIntervalMs = 5000L;

    public Set<String> getRegisteredCollections() {
        return collections;
    }

    public Set<Descriptors.Descriptor> getRegisteredDescriptors() {
        return descriptors;
    }

    public void registerCollection(String resourceEnum) {
        this.collections.add(resourceEnum);
    }

    public void registerDescriptor(Collection<Descriptors.Descriptor> descriptors) {
        this.descriptors.addAll(descriptors);
    }

    public long getStatusCheckIntervalMs() {
        return statusCheckIntervalMs;
    }

    public void setStatusCheckIntervalMs(long statusCheckIntervalMs) {
        this.statusCheckIntervalMs = statusCheckIntervalMs;
    }
}
