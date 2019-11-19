package com.netease.cloud.nsf.cache;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventHandler<T extends HasMetadata> {

    private List<ResourceUpdatedListener> resourceAddedListeners = new ArrayList<>();

    private List<ResourceUpdatedListener> resourceUpdatedListeners = new ArrayList<>();

    private List<ResourceUpdatedListener> resourceDeleteListeners = new ArrayList<>();

    private int THREAD_POOL_CORE_SIZE = 5;
    private int THREAD_POOL_MAX_SIZE = 10;
    private int THREAD_POOL_KEEP_ALIVE_TIME = 2000;


    private ExecutorService callBackThreadPool =
            new ThreadPoolExecutor(
                    THREAD_POOL_CORE_SIZE,
                    THREAD_POOL_MAX_SIZE,
                    THREAD_POOL_KEEP_ALIVE_TIME,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(),
                    new ThreadFactoryBuilder().setNameFormat("resource-event-callBack-pool-%d").build()
            );


    void handleUpdate(ResourceUpdateEvent event) {
        if (resourceUpdatedListeners.isEmpty()) {
            return;
        }
        callBackThreadPool.execute(() -> {
            for (ResourceUpdatedListener listener : resourceUpdatedListeners) {
                listener.notify(event);
            }
        });
    }

    void handleCreate(ResourceUpdateEvent event) {
        if (resourceAddedListeners.isEmpty()) {
            return;
        }
        callBackThreadPool.execute(() -> {
            for (ResourceUpdatedListener listener : resourceAddedListeners) {
                listener.notify(event);
            }
        });
    }

    void handleDelete(ResourceUpdateEvent event) {
        if (resourceDeleteListeners.isEmpty()) {
            return;
        }
        callBackThreadPool.execute(() -> {
            for (ResourceUpdatedListener listener : resourceDeleteListeners) {
                listener.notify(event);
            }
        });
    }

    public void subscribeAddedListener(ResourceUpdatedListener resourceAddedListener) {
        this.resourceAddedListeners.add(resourceAddedListener);
    }

    public void subscribeUpdatedListener(ResourceUpdatedListener resourceUpdatedListener) {
        this.resourceUpdatedListeners.add(resourceUpdatedListener);
    }

    public void subscribeDeletedListener(ResourceUpdatedListener resourceDeleteListener) {
        this.resourceDeleteListeners.add(resourceDeleteListener);
    }


}