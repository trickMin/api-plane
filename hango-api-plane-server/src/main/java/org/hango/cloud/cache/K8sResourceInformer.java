package org.hango.cloud.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import org.hango.cloud.core.editor.ResourceType;
import org.hango.cloud.core.k8s.K8sResourceEnum;
import org.hango.cloud.core.k8s.K8sResourceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
* @Author: zhufengwei.sx
* @Date: 2022/8/26 14:41
**/
public class K8sResourceInformer {

    private static final Logger log = LoggerFactory.getLogger(K8sResourceInformer.class);

    private static final int EVENT_QUEUE_SIZE_FACTOR = 500;
    private static final int MAX_PROCESSOR_THREAD = 10;
    private AtomicInteger processorIndex;
    private static final String UPDATE_EVENT = "MODIFIED";
    private static final String CREATE_EVENT = "ADDED";
    private static final String DELETE_EVENT = "DELETED";

    private String namespace;
    private String resourceKind;
    private RawCustomResourceOperationsImpl rawCustomResourceOperations;

    private ResourceStore store;

    public LinkedBlockingQueue<ResourceEvent> eventQueue;

    private ScheduledExecutorService processorIndexUpdatePool;

    //消息处理者
    private ExecutorService eventProcessor;

    public static class Builder {
        private String namespace;
        private String resourceKind;
        private RawCustomResourceOperationsImpl rawCustomResourceOperations;
        private ResourceStore resourceStore;

        public Builder addNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder addResourceKind(String resourceKind) {
            this.resourceKind = resourceKind;
            return this;
        }

        public Builder addResourceStore(ResourceStore resourceStore) {
            this.resourceStore = resourceStore;
            return this;
        }

        public Builder addRawCustomResourceOperationsImpl(RawCustomResourceOperationsImpl rawCustomResourceOperations) {
            this.rawCustomResourceOperations = rawCustomResourceOperations;
            return this;
        }


        public K8sResourceInformer build() {
            K8sResourceInformer informer = new K8sResourceInformer();
            informer.resourceKind = this.resourceKind;
            informer.rawCustomResourceOperations = this.rawCustomResourceOperations;
            informer.store = this.resourceStore;
            informer.namespace = this.namespace;
            return informer;
        }

    }

    class ResourceProcessor implements Runnable {

        private final int index;

        public ResourceProcessor(int index) {
            this.index = index;
        }

        private boolean closeProcessor() {
            return this.index > processorIndex.get();
        }

        @Override
        public void run() {
            try {
                while (!closeProcessor()){
                    ResourceEvent event = eventQueue.take();
                    String eventType = event.getEventType();
                    K8sResourceGenerator gen = null;
                    try {
                        gen = K8sResourceGenerator.newInstance(event.getResource(), ResourceType.JSON);
                    } catch (Exception e) {
                        log.error("parse resouce execption, resource:{}", event.getResource(), e);
                        continue;
                    }
                    K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
                    HasMetadata resource = gen.object(resourceEnum.mappingType());
                    String name = gen.getName();
                    String resourceVersion = gen.getResourceVersion();
                    boolean current = concurrent();
                    switch (eventType) {
                        case UPDATE_EVENT:
                            store.update(name, resource, resourceVersion, current);
                            break;
                        case CREATE_EVENT:
                            store.add(name, resource, resourceVersion, current);
                            break;
                        case DELETE_EVENT:
                            store.delete(name, resourceVersion, current);
                            break;
                        default:
                            log.debug("unknown event ");
                    }
                }
                log.info("Get resource thread closed, index:{}", index);
            } catch (InterruptedException e) {
                log.error("Get resource update event from queue error, index:{}", index, e);
            }

        }
    }

    private boolean concurrent(){
        return processorIndex.get() > 1;
    }


    public void start() {
        //初始化
        init();
        //开启处理线程
        processorIndexUpdatePool.scheduleAtFixedRate(() -> {
                    if (processorIndex.get() * EVENT_QUEUE_SIZE_FACTOR <= eventQueue.size() || processorIndex.get() == 0) {
                        processorIndex.incrementAndGet();
                        if (processorIndex.get() < MAX_PROCESSOR_THREAD) {
                            int index = processorIndex.get();
                            eventProcessor.execute(new ResourceProcessor(index));
                            if (index > 1){
                                log.warn("{} start new thread to process event , index is {}",resourceKind, index);
                            }

                        }

                    }else if (processorIndex.get() * EVENT_QUEUE_SIZE_FACTOR > eventQueue.size() && processorIndex.get() > 1){
                        processorIndex.decrementAndGet();
                    }
                },
                0,
                5,
                TimeUnit.MINUTES);
        //开启watch线程
        eventProcessor.execute(new ResourceWatch(rawCustomResourceOperations));
    }

    private void init(){
        processorIndex = new AtomicInteger(0);
        this.eventQueue = new LinkedBlockingQueue<>();
        this.processorIndexUpdatePool = Executors.newScheduledThreadPool(1);
        this.eventProcessor = Executors.newCachedThreadPool();
    }

    class ResourceWatch implements Runnable{
        private final RawCustomResourceOperationsImpl rawCustomResourceOperations;

        ResourceWatch(@NotNull RawCustomResourceOperationsImpl rawCustomResourceOperations){
            this.rawCustomResourceOperations = rawCustomResourceOperations;
        }
        @Override
        public void run() {
            try {
                rawCustomResourceOperations.watch(namespace, new Watcher<String>() {
                    @Override
                    public void eventReceived(Action action, String resource) {
                        eventQueue.offer(new ResourceEvent(action.name(), resource));
                    }

                    @Override
                    public void onClose(KubernetesClientException cause) {

                    }
                });
            } catch (IOException e) {
                log.error("watch event error", e);
            }
        }
    }


}
