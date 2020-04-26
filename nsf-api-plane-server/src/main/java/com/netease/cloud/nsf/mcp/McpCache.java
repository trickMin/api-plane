package com.netease.cloud.nsf.mcp;

import istio.mcp.nsf.SnapshotOuterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/10
 **/
public class McpCache implements McpResourceDistributor, McpResourceWatcher {
    private static final Logger logger = LoggerFactory.getLogger(McpCache.class);

    private SnapshotOuterClass.Snapshot snapshot;

    private final List<Connection> connections = new ArrayList<>();

    @Override
    public void watch(Connection connection) {
        synchronized (this) {
            this.connections.add(connection);
            if (Objects.nonNull(snapshot)) {
                distribute(connection, snapshot);
            }
        }
    }

    @Override
    public void release(Connection connection) {
        synchronized (this) {
            this.connections.remove(connection);
        }
    }

    @Override
    public void setSnapshot(SnapshotOuterClass.Snapshot snapshot) {
        synchronized (this) {
            this.snapshot = snapshot;
            for (Connection connection : connections) {
                distribute(connection, snapshot);
            }
        }
    }

    @Override
    public void clearSnapshot() {
        synchronized (this) {
            snapshot = null;
        }
    }

    private void distribute(Connection connection, SnapshotOuterClass.Snapshot snapshot) {
        List<String> collections = new ArrayList<>(snapshot.getResourcesMap().keySet());
        for (String collection : collections) {
            if (connection.isSubscribeCollection(collection)) {
                WatchResponse response = new WatchResponse(snapshot.getVersion(), snapshot.getResourcesMap().get(collection));
                connection.push(response);
            }
        }
    }
}
