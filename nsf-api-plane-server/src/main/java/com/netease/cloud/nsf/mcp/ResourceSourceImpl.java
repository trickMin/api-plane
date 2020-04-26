package com.netease.cloud.nsf.mcp;

import io.grpc.stub.StreamObserver;
import istio.mcp.v1alpha1.Mcp;
import istio.mcp.v1alpha1.ResourceSourceGrpc;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/10
 **/
public class ResourceSourceImpl extends ResourceSourceGrpc.ResourceSourceImplBase {

    private McpResourceWatcher watcher;
    private McpOptions options;


    public ResourceSourceImpl(McpResourceWatcher watcher, McpOptions options) {
        this.watcher = watcher;
        this.options = options;
    }

    @Override
    public StreamObserver<Mcp.RequestResources> establishResourceStream(StreamObserver<Mcp.Resources> resp) {
        Connection conn = newConnection(resp);
        watcher.watch(conn);
        return new StreamObserver<Mcp.RequestResources>() {
            @Override
            public void onNext(Mcp.RequestResources req) {
                conn.processClientRequest(req);
            }

            @Override
            public void onError(Throwable throwable) {
                watcher.release(conn);
            }

            @Override
            public void onCompleted() {
                watcher.release(conn);
            }
        };
    }

    private Connection newConnection(StreamObserver<Mcp.Resources> resp) {
        return new Connection(resp, options);
    }
}
