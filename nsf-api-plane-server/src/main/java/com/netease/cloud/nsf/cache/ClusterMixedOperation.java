package com.netease.cloud.nsf.cache;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.client.dsl.MixedOperation;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author zhangzihao
 */
public class ClusterMixedOperation implements MixedOperation {

    private String clusterId;
    private boolean watchResource;
    private MixedOperation delegate;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public ClusterMixedOperation(String clusterId, MixedOperation delegate,boolean watchResource) {
        this.clusterId = clusterId;
        this.delegate = delegate;
        this.watchResource = watchResource;
    }

    public boolean isWatchResource() {
        return watchResource;
    }

    public void setWatchResource(boolean watchResource) {
        this.watchResource = watchResource;
    }

    @Override
    public Object withGracePeriod(long gracePeriodSeconds) {
        return delegate.withGracePeriod(gracePeriodSeconds);
    }

    @Override
    public Object inAnyNamespace() {
        return delegate.inAnyNamespace();
    }

    @Override
    public Object createOrReplace(Object[] item) {
        return delegate.createOrReplace(item);
    }

    @Override
    public Object createOrReplaceWithNew() {
        return delegate.createOrReplaceWithNew();
    }

    @Override
    public Object create(Object[] item) {
        return delegate.create(item);
    }

    @Override
    public Object createNew() {
        return delegate.createNew();
    }

    @Override
    public Object delete() {
        return delegate.delete();
    }

    @Override
    public Object withLabels(Map labels) {
        return delegate.withLabels(labels);
    }

    @Override
    public Object withoutLabels(Map labels) {
        return delegate.withoutLabels(labels);
    }

    @Override
    public Object withLabelIn(String key, String... values) {
        return delegate.withLabelIn(key, values);
    }

    @Override
    public Object withLabelNotIn(String key, String... values) {
        return delegate.withLabelNotIn(key, values);
    }

    @Override
    public Object withLabel(String key, String value) {
        return delegate.withLabel(key, value);
    }

    @Override
    public Object withLabel(String key) {
        return delegate.withLabel(key);
    }

    @Override
    public Object withoutLabel(String key, String value) {
        return delegate.withoutLabel(key, value);
    }

    @Override
    public Object withoutLabel(String key) {
        return delegate.withoutLabel(key);
    }

    @Override
    public Object withField(String key, String value) {
        return delegate.withField(key, value);
    }

    @Override
    public Object withLabelSelector(LabelSelector selector) {
        return delegate.withLabelSelector(selector);
    }

    @Override
    public Object withFields(Map labels) {
        return delegate.withFields(labels);
    }

    @Override
    public Object list() {
        return delegate.list();
    }

    @Override
    public Object list(Integer limitVal, String continueVal) {
        return delegate.list(limitVal, continueVal);
    }

    @Override
    public Object load(InputStream is) {
        return delegate.load(is);
    }

    @Override
    public Object load(URL url) {
        return delegate.load(url);
    }

    @Override
    public Object load(File file) {
        return delegate.load(file);
    }

    @Override
    public Object load(String path) {
        return delegate.load(path);
    }

    @Override
    public Object delete(Object[] items) {
        return delegate.delete(items);
    }

    @Override
    public Object delete(List items) {
        return delegate.delete(items);
    }

    @Override
    public Object withName(String name) {
        return delegate.withName(name);
    }

    @Override
    public Object inNamespace(String name) {
        return delegate.inNamespace(name);
    }

    @Override
    public Object withResourceVersion(String resourceVersion) {
        return delegate.withResourceVersion(resourceVersion);
    }

    @Override
    public Object watch(Object watcher) {
        return delegate.watch(watcher);
    }

    @Override
    public Object watch(String resourceVersion, Object watcher) {
        return delegate.watch(resourceVersion, watcher);
    }
}
