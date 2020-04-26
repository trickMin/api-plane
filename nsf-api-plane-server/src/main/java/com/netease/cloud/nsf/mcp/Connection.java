package com.netease.cloud.nsf.mcp;

import io.grpc.stub.StreamObserver;
import istio.mcp.v1alpha1.Mcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/10
 **/
public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    private final StreamObserver<Mcp.Resources> stream;
    private final McpOptions mcpOptions;
    private final Set<String> subscribeCollection;

    public Connection(StreamObserver<Mcp.Resources> stream, McpOptions mcpOptions) {
        this.stream = stream;
        this.mcpOptions = mcpOptions;
        this.subscribeCollection = new CopyOnWriteArraySet<>();
    }

    public StreamObserver<Mcp.Resources> getStream() {
        return stream;
    }

    public void processClientRequest(Mcp.RequestResources req) {
        if (McpUtils.isTriggerResponse(req)) return;
        String collection = req.getCollection();
        if (StringUtils.isEmpty(req.getResponseNonce())) {
            logger.info("MCP: connection {}: inc={} SUBSCRIBE for {}", this, req.getIncremental(), collection);
            if (!McpUtils.isSupportedCollection(mcpOptions.getRegisteredCollections(), collection)) {
                logger.warn("--MCP: unsupported collection :{}, connection {}", collection, this);
            } else {
                subscribeCollection(collection);
            }
            pushEmpty(collection);
        } else {
            if (req.hasErrorDetail()) {
                // NACK Response
                logger.warn("MCP: connection {}: NACK collection={} with nonce={} error={} inc={}", // nolint: lll
                        this, collection, req.getResponseNonce(), req.getErrorDetail().getMessage(), req.getIncremental());
            } else {
                // ASK Response
                logger.info("MCP: connection {} ACK collection={} with nonce={} inc={}",
                        this, collection, req.getResponseNonce(), req.getIncremental());
            }
        }
    }

    public void push(WatchResponse resp) {
        Mcp.Resources msg = Mcp.Resources.newBuilder(resp.getResource())
                .setSystemVersionInfo(String.format("Snapshot:[%s],Resource:[%s]", resp.getSnapshotVersion(), resp.getResource().getSystemVersionInfo()))
                .setIncremental(false)
                .setNonce(String.valueOf(UUID.randomUUID()))
                .build();

        getStream().onNext(msg);
        logger.debug("MCP: connection {}: SEND collection={} version={} nonce={} inc={}",
                this, msg.getCollection(), msg.getSystemVersionInfo(), msg.getNonce(), msg.getIncremental());
    }

    public void pushEmpty(String collection) {
        Mcp.Resources resources = Mcp.Resources.newBuilder().setCollection(collection).build();
        WatchResponse response = new WatchResponse("empty", resources);
        push(response);
    }

    public void subscribeCollection(String collection) {
        this.subscribeCollection.add(collection);
    }

    public boolean isSubscribeCollection(String collection) {
        return this.subscribeCollection.contains(collection);
    }
}
