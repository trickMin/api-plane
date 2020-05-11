package com.netease.cloud.nsf.mcp;

import com.google.protobuf.Descriptors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/17
 **/
public class McpOptions {
    // 注册支持的collection类型
    private Set<String> snapshotCollections = new HashSet<>();
    // 注册descriptor，用于初始化序列化工具
    private Set<Descriptors.Descriptor> descriptors = new HashSet<>();
    // ratelimit server cluster地址
    private Set<String> rlsCluster = new HashSet<>();
    // status检查间隔
    private long statusCheckIntervalMs = 5000L;

    public Set<String> getSnapshotCollections() {
        return snapshotCollections;
    }

    public Set<Descriptors.Descriptor> getRegisteredDescriptors() {
        return descriptors;
    }

    public Set<String> getRegisteredRlsCluster() {
        return rlsCluster;
    }

    public void registerSnapshotCollection(String collection) {
        this.snapshotCollections.add(collection);
    }

    public void registerDescriptor(Collection<Descriptors.Descriptor> descriptors) {
        this.descriptors.addAll(descriptors);
    }

    public void registerRls(String address) {
        this.rlsCluster.add(address);
    }

    public long getStatusCheckIntervalMs() {
        return statusCheckIntervalMs;
    }

    public void setStatusCheckIntervalMs(long statusCheckIntervalMs) {
        this.statusCheckIntervalMs = statusCheckIntervalMs;
    }
}
