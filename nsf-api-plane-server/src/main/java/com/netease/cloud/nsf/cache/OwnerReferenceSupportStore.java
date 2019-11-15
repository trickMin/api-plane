package com.netease.cloud.nsf.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.k8s.K8sResourceEnum.*;


/**
 * 对ResourceStore 进行了包装，在处理资源的crud操作时，会提取出资源的ownerReference信息，并建立索引，记录了指定
 * 资源所属的关联资源。如Deploy下的所属的ReplicasSet，ReplicasSet下所属的Pod，提供了资源之间关联查询的能力。
 *
 * @author zhangzihao
 */
public class OwnerReferenceSupportStore<T extends HasMetadata> implements Store<T> {

    private Map<String, List<T>> ownerReference = new HashMap<>();

    private ResourceStore resourceStore;

    public OwnerReferenceSupportStore(ResourceStore resourceStore) {
        this.resourceStore = resourceStore;
    }

    @Override
    public T get(String kind, String namespace, String name) {
        return (T) resourceStore.get(kind, namespace, name);
    }

    @Override
    public void add(String kind, String namespace, String name, T obj) {
        resourceStore.add(kind, namespace, name, obj);
        List<String> ownerReferenceNames = getOwnerName(obj);
        // 添加新的ownerReference索引
        synchronized (OwnerReferenceSupportStore.class) {
            ownerReferenceNames.forEach(n -> {
                Objects.requireNonNull(ownerReference.computeIfAbsent(n,(k)->new ArrayList<>())).add(obj);
            });
        }

    }

    @Override
    public void update(String kind, String namespace, String name, T obj) {
        resourceStore.update(kind, namespace, name, obj);
        // TODO: 2019-11-05 是否更新ownerReference信息
    }

    @Override
    public T delete(String kind, String namespace, String name) {
        T oldValue = (T) resourceStore.delete(kind, namespace, name);
        List<String> ownerReferenceNames = getOwnerName(oldValue);
        // 删除旧的ownerReference索引
        synchronized (OwnerReferenceSupportStore.class) {
            ownerReferenceNames.forEach(n -> Objects.requireNonNull(ownerReference
                    .computeIfAbsent(n, (k)->new ArrayList<>()))
                    .remove(oldValue));
        }
        return oldValue;
    }

    @Override
    public List<T> list() {
        return resourceStore.list();
    }

    @Override
    public List<T> listByKind(String kind) {
        return resourceStore.listByKind(kind);
    }

    @Override
    public List<T> listByKindAndNamespace(String kind, String namespace) {
        return resourceStore.listByKindAndNamespace(kind, namespace);
    }

    /**
     * 全量更新指定类型的资源信息，同时更新OwnerReference信息
     *
     * @param resourceMap 最新获取的资源信息
     * @param kind        资源类型
     */
    @Override
    public void replaceByKind(Map<String, Map<String, T>> resourceMap, String kind) {

        synchronized (OwnerReferenceSupportStore.class) {
            // 先清除原有的OwnerReference信息
            List<T> oldResource = listByKind(kind);
            Map<String, List<T>> tmpOwnerReference = new HashMap<>(ownerReference);
            oldResource.forEach(t -> getOwnerName(t)
                    .forEach(name -> Objects.requireNonNull(tmpOwnerReference
                            .computeIfAbsent(name, (k)->new ArrayList<>()))
                            .remove(t)));
            // 添加新的OwnerReference信息
            if (resourceMap != null && !resourceMap.isEmpty()) {
                List<T> newResource = resourceMap.entrySet()
                        .stream()
                        .flatMap(t -> t.getValue().values().stream())
                        .collect(Collectors.toList());
                newResource.forEach(t -> getOwnerName(t).forEach(name -> Objects
                        .requireNonNull(tmpOwnerReference
                                .computeIfAbsent(name,(k)->new ArrayList<>()))
                        .add(t)));
            }
            // 过滤掉那些value值为空key
            ownerReference = tmpOwnerReference.entrySet()
                    .stream()
                    .filter(entry -> CollectionUtils.isEmpty(entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            Map.Entry::getValue));
            resourceStore.replaceByKind(resourceMap, kind);
        }

    }

    @Override
    public List<T> listByNamespace(String namespace) {
        return resourceStore.listByNamespace(namespace);
    }

    /**
     * 给定Pod向上查询其所属的工作负载列表。会经过Pod->ReplicasSet->Deploy这样的路径递归的查询全部
     * 满足条件的负载资源
     *
     * @param obj Pod资源
     * @return 负载资源列表
     */
    public List<T> listLoadByPod(T obj) {
        List<OwnerReference> ownerReferences = obj.getMetadata().getOwnerReferences();
        if (CollectionUtils.isEmpty(ownerReferences)) {
            return new ArrayList<>();
        }
        List<T> resourceByKind = new ArrayList<>();
        for (OwnerReference reference : ownerReferences) {
            T resource = get(reference.getKind(), obj.getMetadata().getNamespace(), reference.getName());
            if (resource == null) {
                continue;
            }
            if (Deployment.name().equals(reference.getKind())
                    || StatefulSet.name().equals(reference.getKind())) {
                resourceByKind.add(resource);
            } else {
                resourceByKind.addAll(listLoadByPod(resource));
            }
        }
        return resourceByKind;
    }


    /**
     * 通过OwnerReference信息获取与之关联的下属资源。如给定deploy查询Pod，会经过Deploy->ReplicasSet->Pod这样的路径递归的查询全部
     * 满足条件的Pod资源
     *
     * @param kind 查询的资源类型
     * @param obj  OwnerReference资源对象
     * @return 满足条件的资源列表
     */
    public List<T> listResourceByOwnerReference(String kind, T obj) {
        List<T> resourceList = listByOwnerReferenceInfo(obj.getMetadata().getNamespace(), obj.getKind(), obj.getMetadata().getName());
        if (CollectionUtils.isEmpty(resourceList)) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>();
        for (T resource : resourceList) {
            if (resource == null) {
                continue;
            }
            if (kind.equals(resource.getKind())) {
                result.add(resource);
            } else {
                result.addAll(listResourceByOwnerReference(kind, resource));
            }
        }
        return result;
    }

    public List<T> getFilterByLabel(List<T> resourceList, Map<String, String> keyValues) {
        return resourceList.stream()
                .filter(i ->labelsMatch(keyValues,i.getMetadata().getLabels()))
                .collect(Collectors.toList());
    }

    private List<T> listByOwnerReferenceInfo(String namespace, String kind, String name) {
        String referenceKey = kind + "/" + namespace + "/" + name;
        return ownerReference.get(referenceKey);
    }

    /**
     * 从资源中获取OwnerReference名称列表,用于建立索引，名称格式为kind/namespace/name
     *
     * @param obj 资源对象
     * @return 名称列表
     */
    private List<String> getOwnerName(T obj) {
        List<OwnerReference> ownerReferences = obj.getMetadata().getOwnerReferences();
        if (CollectionUtils.isEmpty(ownerReferences)) {
            return new ArrayList<>();
        }
        return ownerReferences.stream()
                .filter(i -> i.getKind() != null && i.getName() != null)
                .map(i -> i.getKind() + "/" + obj.getMetadata().getNamespace() + "/" + i.getName())
                .collect(Collectors.toList());
    }


    private boolean labelsMatch(Map<String, String> keyValues, Map<String, String> label) {
        if (label == null || label.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, String> entry : keyValues.entrySet()) {
            if (label.get(entry.getKey()) == null || !label.get(entry.getKey()).equals(entry.getValue())) {
                return false;
            }

        }
        return true;
    }


}
