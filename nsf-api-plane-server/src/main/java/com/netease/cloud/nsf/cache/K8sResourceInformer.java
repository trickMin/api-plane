package com.netease.cloud.nsf.cache;

import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhangzihao
 */

public class K8sResourceInformer<T extends HasMetadata> implements Informer {

    private static final Logger log = LoggerFactory.getLogger(K8sResourceInformer.class);

    private static final int EVENT_QUEUE_SIZE_FACTOR = 500;
    private static final int MAX_QUEUE_SIZE = 10000;
    private static final int MAX_PROCESSOR_THREAD = 20;
    private static AtomicInteger processorIndex = new AtomicInteger(0);
    private static final String UPDATE_EVENT = "MODIFIED";
    private static final String CREATE_EVENT = "ADDED";
    private static final String DELETE_EVENT = "DELETED";
    private Map<String, OwnerReferenceSupportStore<T>> clusterStore = new HashMap<>();
    private K8sResourceEnum resourceKind;
    private EventHandler<HasMetadata> handler;
    private List<ResourceFilter> includeFilter;
    private List<ResourceFilter> excludeFilter;
    private List<MixedOperation> mixedOperationList;
    private MultiClusterK8sClient multiClusterK8sClient;


    private ArrayBlockingQueue<ResourceUpdateEvent> eventQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE,false);

    private ExecutorService eventProcessor = Executors.newCachedThreadPool();
    private ScheduledExecutorService processorIndexUpdatePool = Executors.newScheduledThreadPool(1);


    public static class Builder {
        private EventHandler<HasMetadata> handler = new EventHandler<>();
        private K8sResourceEnum resourceKind;
        private List<ResourceFilter> includeFilter = new LinkedList<>();
        private List<ResourceFilter> excludeFilter = new LinkedList<>();
        private List<MixedOperation> mixedOperation;
        private MultiClusterK8sClient multiClusterK8sClient;

        public Builder addUpdateListener(ResourceUpdatedListener listener) {
            this.handler.subscribeUpdatedListener(listener);
            return this;
        }

        public Builder addCreateListener(ResourceUpdatedListener<ResourceUpdateEvent<HasMetadata>> listener) {
            this.handler.subscribeAddedListener(listener);
            return this;
        }

        public Builder addDeleteListener(ResourceUpdatedListener<ResourceUpdateEvent<HasMetadata>> listener) {
            this.handler.subscribeDeletedListener(listener);
            return this;
        }

        public Builder addIncludeFilter(ResourceFilter filter) {
            this.includeFilter.add(filter);
            return this;
        }

        public Builder addExcludeFilter(ResourceFilter filter) {
            this.excludeFilter.add(filter);
            return this;
        }

        public Builder addResourceKind(K8sResourceEnum resourceKind) {
            this.resourceKind = resourceKind;
            return this;
        }

        public Builder addMixedOperation(List<MixedOperation> mpList) {
            this.mixedOperation = mpList;
            return this;
        }

        public Builder addHttpK8sClient(MultiClusterK8sClient multiClusterK8sClient) {
            this.multiClusterK8sClient = multiClusterK8sClient;
            return this;
        }


        public K8sResourceInformer<HasMetadata> build() {
            K8sResourceInformer<HasMetadata> informer = new K8sResourceInformer<>();
            informer.resourceKind = this.resourceKind;
            informer.handler = this.handler;
            informer.includeFilter = this.includeFilter;
            informer.excludeFilter = this.excludeFilter;
            informer.mixedOperationList = this.mixedOperation;
            informer.multiClusterK8sClient = this.multiClusterK8sClient;
            return informer;
        }

    }

    class ResourceProcessor implements Runnable {

        private int index;

        public ResourceProcessor(int index) {
            this.index = index;
        }

        private boolean closeProcessor() {
            if (this.index > processorIndex.get()) {
                return true;
            }
            return false;
        }

        @Override
        public void run() {
            try {
                while (!closeProcessor()) {
                    ResourceUpdateEvent event = eventQueue.take();
                    String eventType = event.getEventType();
                    switch (eventType) {
                        case UPDATE_EVENT:
                            updateResource((T) event.getResourceObject(), event.getClusterId());
                            handler.handleUpdate(event);
                            break;
                        case CREATE_EVENT:
                            addResource((T) event.getResourceObject(), event.getClusterId());
                            handler.handleCreate(event);
                            break;
                        case DELETE_EVENT:
                            deleteResource((T) event.getResourceObject(), event.getClusterId());
                            handler.handleDelete(event);
                            break;
                        default:
                            log.warn("unknown event ");
                    }
                }
                log.info("Resource processor end with index {}", index);
            } catch (InterruptedException e) {
                log.error("Get resource update event from queue error", e);
            }

        }
    }

    @Override
    public void start() {

        processorIndexUpdatePool.scheduleAtFixedRate(() -> {
                    if (processorIndex.get() * EVENT_QUEUE_SIZE_FACTOR <= eventQueue.size()
                            || processorIndex.get() == 0) {
                        processorIndex.incrementAndGet();
                        if (processorIndex.get() < MAX_PROCESSOR_THREAD) {
                            int index = processorIndex.get();
                            eventProcessor.execute(new ResourceProcessor(index));
                            log.info("start new thread to process event , index is {}",index);
                        }

                    }else if (processorIndex.get() * EVENT_QUEUE_SIZE_FACTOR > eventQueue.size()
                            && processorIndex.get() > 1){
                        processorIndex.decrementAndGet();
                    }

                },
                0,
                5,
                TimeUnit.MINUTES);
        for (MixedOperation mixedOperation : mixedOperationList) {
            eventProcessor.execute(() -> {
                mixedOperation.watch(new Watcher<T>() {
                    @Override
                    public void eventReceived(Action action, T t) {
                        addEvent(t, action.name(), ((ClusterMixedOperation) mixedOperation).getClusterId());
                    }

                    @Override
                    public void onClose(KubernetesClientException e) {

                    }
                });
            });

        }
    }


    public void addResource(T obj, String clusterId) {
        String resourceName = obj.getMetadata().getName();
        String namespace = obj.getMetadata().getNamespace();
        Store store = getStoreByClusterId(clusterId);
        store.add(resourceKind.name(), namespace, resourceName, obj);
    }

    public void updateResource(T obj, String clusterId) {
        String resourceName = obj.getMetadata().getName();
        String namespace = obj.getMetadata().getNamespace();
        Store store = getStoreByClusterId(clusterId);
        store.update(resourceKind.name(), namespace, resourceName, obj);
    }

    public void deleteResource(T obj, String clusterId) {
        String resourceName = obj.getMetadata().getName();
        String namespace = obj.getMetadata().getNamespace();
        Store store = getStoreByClusterId(clusterId);
        store.delete(resourceKind.name(), namespace, resourceName);
    }

    private Store getStoreByClusterId(String clusterId) {

        return ResourceStoreFactory.getResourceStore(clusterId);
    }


    @Override
    public void replaceResource() {
        // TODO: 2019-11-04 从k8s获取informer所监听资源列表并更新本地
        multiClusterK8sClient.getAllClients().forEach((cluster, clientSet) -> {
                    KubernetesClient httpClient = clientSet.k8sClient;
                    List<T> objectList = httpClient.getObjectList(resourceKind.name(), "");
                    Map resourceMap = buildResourceMapByKind(objectList);
                    getStoreByClusterId(cluster).replaceByKind(resourceMap, resourceKind.name());
                    log.info("resourceList update clusterId[{}] kind[{}]", cluster, resourceKind.name());
                }
        );
//        for (ClusterResourceList kubernetesList : kubernetesLists) {
//            String clusterId = kubernetesList.getCluster();
//            Map resourceMap = buildResourceMapByKind(kubernetesList.getItems());
//            getStoreByClusterId(clusterId).replaceByKind(resourceMap, resourceKind.name());
//            log.info("resourceList update clusterId[{}] kind[{}]", clusterId, resourceKind.name());
//        }
    }

    public void addEvent(T obj, String type, String clusterId) {
        if (!isValidResource(obj)) {
            log.warn("invalid resource");
            return;
        }
        if (CollectionUtils.isEmpty(excludeFilter)) {
            for (ResourceFilter filter : excludeFilter) {
                if (filter.match(obj)) {
                    return;
                }
            }
        }
        if (CollectionUtils.isEmpty(includeFilter)) {
            for (ResourceFilter filter : includeFilter) {
                if (!filter.match(obj)) {
                    return;
                }
            }
        }

        ResourceUpdateEvent resourceUpdateEvent = new ResourceUpdateEvent.Builder<T>()
                .addCluster(clusterId)
                .addEventType(type)
                .addLabels(obj.getMetadata().getLabels())
                .addNamespace(obj.getMetadata().getNamespace())
                .addResourceObj(obj)
                .addName(obj.getMetadata().getName())
                .build();
        eventQueue.add(resourceUpdateEvent);
    }


    private boolean isValidResource(T obj) {
        if (StringUtils.isBlank(obj.getKind())) {
            log.warn("Resource can't be null ");
            return false;
        }
        if (obj.getMetadata() == null) {
            log.warn("Resource metadata can't be null");
            return false;
        }
        if (StringUtils.isBlank(obj.getMetadata().getNamespace())) {
            log.warn("Resource namespace can't be null ");
            return false;
        }
        if (StringUtils.isBlank(obj.getMetadata().getName())) {
            log.warn("Resource name can't be null");
            return false;
        }
//        if (obj.getMetadata().getLabels() == null || obj.getMetadata().getLabels().isEmpty()) {
//            log.warn("Resource name can't be null");
//            return false;
//        }
        return true;
    }


    private Map<String, Map<String, T>> buildResourceMapByKind(List<T> resourceList) {
        Map<String, Map<String, T>> result = new HashMap<>();
        for (T obj : resourceList) {
            String namespace = obj.getMetadata().getNamespace();
            String name = obj.getMetadata().getName();
            Map<String, T> objMap = result.computeIfAbsent(namespace, (k) -> new HashMap<>());
            objMap.put(name, obj);
        }
        return result;
    }

}
