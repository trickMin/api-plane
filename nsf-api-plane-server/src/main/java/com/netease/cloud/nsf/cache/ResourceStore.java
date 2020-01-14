package com.netease.cloud.nsf.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.springframework.util.StringUtils;

import java.util.*;
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
            return null;
            //throw new ApiPlaneException("the server doesn't have a resource type " + kind, 404);
        }
        Map<String, T> resourceByKindAndNamespace = resourceByKind.get(namespace);
        if (resourceByKindAndNamespace == null || resourceByKindAndNamespace.isEmpty()) {
           // throw new ApiPlaneException("No resources found", 404);
            return null;
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

    public List<String> listNamespaces() {
        if (resourceStore ==null || resourceStore.isEmpty()){
            return new ArrayList<>();
        }
        Set<String> namespaceSet = new HashSet<>();
        Collection<Map<String, Map<String, T>>> values = resourceStore.values();
        for (Map<String, Map<String, T>> resourceByNamespace : values) {
            namespaceSet.addAll(resourceByNamespace.keySet());
        }
        return new ArrayList<>(namespaceSet);
    }
}
