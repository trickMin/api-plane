package com.netease.cloud.nsf.mcp.ratelimit;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.mcp.McpMarshaller;
import com.netease.cloud.nsf.mcp.McpOptions;
import com.netease.cloud.nsf.mcp.McpResourceEnum;
import com.netease.cloud.nsf.mcp.dao.ResourceDao;
import com.netease.cloud.nsf.mcp.dao.meta.Resource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratelimit.config.Config;
import ratelimit.config.RatelimitConfigServiceGrpc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/5/7
 **/
public class RlsClusterClient {
    private static final Logger logger = LoggerFactory.getLogger(RlsClusterClient.class);

    private Set<RatelimitConfigServiceGrpc.RatelimitConfigServiceStub> stubs;
    private ResourceDao resourceDao;
    private McpMarshaller marshaller;

    public RlsClusterClient(McpOptions mcpOptions, ResourceDao resourceDao, McpMarshaller marshaller) {
        Set<RatelimitConfigServiceGrpc.RatelimitConfigServiceStub> stubs = new HashSet<>();
        for (String address : mcpOptions.getRegisteredRlsCluster()) {
            String[] hostPort = StringUtils.split(address, ":");
            if (hostPort.length == 2) {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(hostPort[0], Integer.parseInt(hostPort[1])).usePlaintext().build();
                stubs.add(RatelimitConfigServiceGrpc.newStub(channel));
            }
        }
        this.stubs = stubs;
        this.resourceDao = resourceDao;
        this.marshaller = marshaller;
    }

    public void sync() {
        List<ratelimit.config.Config.RateLimitConf> configs = new ArrayList<>();
        List<Resource> resourceList = resourceDao.list(McpResourceEnum.ConfigMap.getCollection());
        for (Resource resource : resourceList) {
            //todo: 判断是否是ratelimit的configmap
            ratelimit.config.Config.RateLimitConf.Builder builder = ratelimit.config.Config.RateLimitConf.newBuilder();
            K8sResourceGenerator gen = K8sResourceGenerator.newInstance(resource.getConfig());
            String yaml = gen.getValue("$.data.['config.yaml']");
            String json = ResourceGenerator.yaml2json(yaml);
            marshaller.merge(json, builder);
            configs.add(builder.build());
        }
        Config.SyncRatelimitConfReq req = Config.SyncRatelimitConfReq.newBuilder().addAllConfigs(configs).build();
        for (RatelimitConfigServiceGrpc.RatelimitConfigServiceStub stub : stubs) {
            stub.syncAllRatelimitConfig(req, new StreamObserver<Config.SyncRatelimitConfResp>() {
                @Override
                public void onNext(Config.SyncRatelimitConfResp syncRatelimitConfResp) {
                    //todo:
                }

                @Override
                public void onError(Throwable throwable) {
                    //todo:
                }

                @Override
                public void onCompleted() {
                    //todo:
                }
            });
        }
    }
}
