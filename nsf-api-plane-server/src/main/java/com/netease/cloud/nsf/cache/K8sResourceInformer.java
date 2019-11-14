package com.netease.cloud.nsf.cache;

import com.netease.cloud.nsf.core.k8s.http.K8sHttpClient;
import com.netease.cloud.nsf.util.K8sResourceEnum;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhangzihao
 */

public class K8sResourceInformer<T extends HasMetadata> implements Informer {

    private static final Logger log = LoggerFactory.getLogger(K8sResourceInformer.class);

    private static final int EVENT_QUEUE_SIZE = 500;
    private static final String UPDATE_EVENT = "MODIFIED";
    private static final String CREATE_EVENT = "ADDED";
    private static final String DELETE_EVENT = "DELETED";
    private Map<String, OwnerReferenceSupportStore<T>> clusterStore = new HashMap<>();
    private K8sResourceEnum resourceKind;
    private EventHandler<HasMetadata> handler;
    private List<ResourceFilter> includeFilter;
    private List<ResourceFilter> excludeFilter;
    private MixedOperation mixedOperation;
    private K8sHttpClient httpClient;


    private ArrayBlockingQueue<ResourceUpdateEvent> eventQueue = new ArrayBlockingQueue<>(EVENT_QUEUE_SIZE, false);

    private ExecutorService eventProcessor = Executors.newFixedThreadPool(2);
    private String DEFAULT_CLUSTER = "default_cluster";


    public static class Builder {
        private EventHandler<HasMetadata> handler = new EventHandler<>();
        private K8sResourceEnum resourceKind;
        private List<ResourceFilter> includeFilter = new LinkedList<>();
        private List<ResourceFilter> excludeFilter = new LinkedList<>();
        private MixedOperation mixedOperation;
        private K8sHttpClient httpClient;

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

        public Builder addMixedOperation(MixedOperation mp) {
            this.mixedOperation = mp;
            return this;
        }

        public Builder addHttpClient(K8sHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public K8sResourceInformer<HasMetadata> build() {
            K8sResourceInformer<HasMetadata> informer = new K8sResourceInformer<>();
            informer.resourceKind = this.resourceKind;
            informer.handler = this.handler;
            informer.includeFilter = this.includeFilter;
            informer.excludeFilter = this.excludeFilter;
            informer.mixedOperation = this.mixedOperation;
            informer.httpClient = this.httpClient;
            return informer;
        }

    }

    @Override
    public void start() {
        eventProcessor.execute(() -> {
            try {
                while (true) {
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
            } catch (InterruptedException e) {
                log.error("Get resource update event from queue error", e);
            }
        });
        eventProcessor.execute(() -> {
            mixedOperation.watch(new Watcher<T>() {
                @Override
                public void eventReceived(Action action, T t) {
                    addEvent(t,action.name(),DEFAULT_CLUSTER);
                }

                @Override
                public void onClose(KubernetesClientException e) {

                }
            });

        });

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


}
