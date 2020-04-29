package com.netease.cloud.nsf.mcp.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public class StatusMonitorImpl implements StatusMonitor {
    private static final Logger logger = LoggerFactory.getLogger(StatusMonitorImpl.class);

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
        // 使用单线程执行分发任务
        this.notifyQueue = Executors.newSingleThreadExecutor();
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
        List<BiConsumer<Event, Status.Property>> handlers = this.handlers.get(property.key);
        if (Objects.nonNull(handlers)) {
            for (BiConsumer<Event, Status.Property> handle : this.handlers.get(property.key)) {
                this.notifyQueue.submit(() -> handle.accept(event, property));
            }
        }
    }
}
