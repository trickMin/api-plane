package com.netease.cloud.nsf.cache;

import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zhangzihao
 */
public class ResourceStore<T extends HasMetadata> implements Store<T> {


    protected Map<String, Map<String, Map<String, T>>> resourceStore = new HashMap<>();


    @Override
    public T get(String kind, String namespace, String name) {
        Map<String, Map<String, T>> resourceByKind = resourceStore.get(kind);
        if (resourceByKind == null || resourceByKind.isEmpty()) {
            throw new ApiPlaneException("the server doesn't have a resource type " + kind, 404);
        }
        Map<String, T> resourceByKindAndNamespace = resourceByKind.get(namespace);
        if (resourceByKindAndNamespace == null || resourceByKindAndNamespace.isEmpty()) {
            throw new ApiPlaneException("No resources found", 404);
        }
        return resourceByKindAndNamespace.get(name);
    }

    @Override
    public void add(String kind, String namespace, String name, T obj) {
        Map<String, T> resourceByKindAndNamespace = getStoreMap(kind, namespace);
        resourceByKindAndNamespace.put(name, obj);
    }

    @Override
    public void update(String kind, String namespace, String name, T obj) {
        Map<String, T> resourceByKindAndNamespace = getStoreMap(kind, namespace);
        resourceByKindAndNamespace.put(name, obj);
    }

    @Override
    public T delete(String kind, String namespace, String name) {
        Map<String, T> resourceByKindAndNamespace = getStoreMap(kind, namespace);
        return resourceByKindAndNamespace.remove(name);
    }


    @Override
    public List<T> list() {
        return resourceStore.entrySet()
                .stream()
                .flatMap(t -> t.getValue().values().stream()
                        .flatMap(i -> i.values().stream()))
                .collect(Collectors.toList());
    }

    @Override
    public List<T> listByNamespace(String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            return new ArrayList<>();
        }
        return resourceStore.entrySet()
                .stream()
                .flatMap(t -> t.getValue().values().stream()
                        .flatMap(i -> i.values().stream()))
                .filter(i -> namespace.equals(i.getMetadata().getNamespace()))
                .collect(Collectors.toList());
    }

    @Override
    public List<T> listByKind(String kind) {
        if (StringUtils.isEmpty(kind) || resourceStore.get(kind) == null) {
            return new ArrayList<>();
        }
        return resourceStore.get(kind).entrySet()
                .stream()
                .flatMap(t -> t.getValue().values().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<T> listByKindAndNamespace(String kind, String namespace) {
        if (StringUtils.isEmpty(kind) || StringUtils.isEmpty(namespace)) {
            return new ArrayList<>();
        }
        Map<String, T> sourceMap = getStoreMap(kind, namespace);
        return new ArrayList<>(sourceMap.values());
    }


    @Override
    public void replaceByKind(Map<String, Map<String, T>> resourceMap, String kind) {
        resourceStore.put(kind, resourceMap);
    }

    private Map<String, T> getStoreMap(String kind, String namespace) {
        Map<String, Map<String, T>> resourceByKind = resourceStore.get(kind);
        if (resourceByKind == null) {
            synchronized (this) {
                resourceStore.putIfAbsent(kind, new HashMap<>(16));
                resourceByKind = resourceStore.get(kind);
            }
        }
        Map<String, T> resourceByKindAndNamespace = resourceByKind.get(namespace);
        if (resourceByKindAndNamespace == null) {
            synchronized (this) {
                resourceByKind.put(namespace, new ConcurrentHashMap<>(16));
                resourceByKindAndNamespace = resourceByKind.get(namespace);
            }
        }
        return resourceByKindAndNamespace;
    }

}
