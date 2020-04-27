package com.netease.cloud.nsf.mcp.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public class StatusMonitorImpl implements StatusMonitor {

    private AtomicReference<Status> status;
    private final Map<String, List<BiConsumer<Event, Status.Property>>> handlers;


    private StatusProductor statusProductor;
    private ScheduledExecutorService timerTask;
    private ExecutorService notifyQueue;
    private long timerIntervalMs;

    public StatusMonitorImpl(long checkIntervalMs, StatusProductor productor) {
        this.status = new AtomicReference<>(new Status(new Status.Property[0]));
        this.handlers = new HashMap<>();
        this.timerTask = Executors.newScheduledThreadPool(1);
        // core:2, max:4, timeout:10s, queue_length: 5, rejectPolicy: AbortPolicy
        this.notifyQueue = new ThreadPoolExecutor(2, 8, 10L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20), new ThreadPoolExecutor.AbortPolicy());
        this.timerIntervalMs = checkIntervalMs;
        this.statusProductor = productor;
    }

    @Override
    public void registerHandler(String key, BiConsumer<Event, Status.Property> handle) {
        if (!handlers.containsKey(key)) {
            handlers.put(key, new ArrayList<>());
        }
        this.handlers.get(key).add(handle);
    }

    @Override
    public void start() {
        this.timerTask.scheduleAtFixedRate(this::process, 0, this.timerIntervalMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        this.timerTask.shutdown();
    }

    private void process() {
        Status oldStatus = this.status.get();
        Status newStatus = statusProductor.product();
        if (this.status.compareAndSet(oldStatus, newStatus)) {
            Status.Difference diff = oldStatus.compare(newStatus);
            for (Status.Property p : diff.getAdd()) {
                notify(Event.ADD, p);
            }
            for (Status.Property p : diff.getUpdate()) {
                notify(Event.UPDATE, p);
            }
            for (Status.Property p : diff.getDelete()) {
                notify(Event.DELETE, p);
            }
        }
    }

    private void notify(StatusMonitor.Event event, Status.Property property) {
        for (BiConsumer<Event, Status.Property> handle : this.handlers.get(property.key)) {
            this.notifyQueue.submit(() -> handle.accept(event, property));
        }
    }
}
