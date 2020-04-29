package com.netease.cloud.nsf.configuration.env;

import com.netease.cloud.nsf.core.GlobalConfig;
import com.netease.cloud.nsf.core.gateway.GatewayIstioModelEngine;
import com.netease.cloud.nsf.core.gateway.service.GatewayConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.core.gateway.service.impl.GatewayConfigManagerImpl;
import com.netease.cloud.nsf.mcp.*;
import com.netease.cloud.nsf.mcp.dao.ResourceDao;
import com.netease.cloud.nsf.mcp.dao.StatusDao;
import com.netease.cloud.nsf.mcp.dao.impl.ResourceDaoImpl;
import com.netease.cloud.nsf.mcp.dao.impl.StatusDaoImpl;
import com.netease.cloud.nsf.mcp.snapshot.DBSnapshotBuilder;
import com.netease.cloud.nsf.mcp.snapshot.SnapshotBuilder;
import com.netease.cloud.nsf.mcp.status.*;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.service.impl.GatewayServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import istio.mcp.nsf.SnapshotOuterClass;
import istio.mcp.v1alpha1.Mcp;
import istio.mcp.v1alpha1.ResourceOuterClass;
import istio.networking.v1alpha3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/10
 **/
@ConditionalOnProperty("nonK8sMode")
@Configuration("nonK8sSupportConfiguration")
public class GatewayNonK8sConfiguration {
    @Value("${mcpPort:8899}")
    private Integer port;

    @Bean
    public McpOptions options() {
        McpOptions options = new McpOptions();
        options.setStatusCheckIntervalMs(1000L);

        options.registerSnapshotCollection(McpResourceEnum.VirtualService.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.Gateway.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.DestinationRule.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.GatewayPlugin.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.PluginManager.getCollection());

        options.registerDescriptor(SnapshotOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(ResourceOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(VirtualServiceOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(DestinationRuleOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(GatewayOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(PluginManagerOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(GatewayPluginOuterClass.getDescriptor().getMessageTypes());
        return options;
    }

    /**
     * Monitor
     */
    @Bean
    public StatusMonitor monitor(McpOptions options, StatusProductor productor, SnapshotBuilder builder, McpResourceDistributor distributor, TransactionTemplate transactionTemplate) {
        StatusMonitor monitor = new StatusMonitorImpl(options.getStatusCheckIntervalMs(), productor);
        monitor.registerHandler(StatusConst.RESOURCES_VERSION, ((event, property) -> {
            Logger logger = LoggerFactory.getLogger(builder.getClass());
            SnapshotOuterClass.Snapshot snapshot = transactionTemplate.execute(new TransactionCallback<SnapshotOuterClass.Snapshot>() {
                @Override
                public SnapshotOuterClass.Snapshot doInTransaction(TransactionStatus transactionStatus) {
                    String thisVersion = property.value;
                    String dbVersion = productor.product().get(StatusConst.RESOURCES_VERSION);
                    if (Objects.equals(thisVersion, dbVersion)) {
                        long start = System.currentTimeMillis();
                        SnapshotOuterClass.Snapshot snapshot = builder.build();
                        logger.info("MCP: SnapshotBuilder: build snapshot for version:[{}], consume:[{}]", property.value, System.currentTimeMillis() - start + "ms");
                        for (Map.Entry<String, Mcp.Resources> entry : snapshot.getResourcesMap().entrySet()) {
                            logger.info("--MCP: SnapshotBuilder: collection:[{}], count:[{}]", entry.getKey(), entry.getValue().getResourcesList().size());
                        }
                        return snapshot;
                    } else {
                        logger.info("MCP: SnapshotBuilder: Skip building snapshots for outdated resource versions:[{}], current version:[{}]", thisVersion, dbVersion);
                        return null;
                    }
                }
            });
            if (Objects.nonNull(snapshot)) {
                distributor.setSnapshot(snapshot);
            }
        }));
        // 启动monitor
        monitor.start();
        return monitor;
    }

    /**
     * Grpc Server
     */
    @Bean
    public Server server(McpResourceWatcher watcher, McpOptions options) throws IOException {
        return ServerBuilder.forPort(port)
                .addService(new ResourceSourceImpl(watcher, options))
                .build()
                .start();
    }

    /**
     * Dao
     */
    @Bean
    public ResourceDao resourceDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new ResourceDaoImpl(namedParameterJdbcTemplate);
    }

    @Bean
    public StatusDao statusDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new StatusDaoImpl(namedParameterJdbcTemplate);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        // 传播级别
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // 隔离级别，容忍幻读
        template.setIsolationLevel(TransactionTemplate.ISOLATION_REPEATABLE_READ);
        // 只读
        template.setReadOnly(true);
        // 事务超时时间
        template.setTimeout(5);
        return template;
    }

    /**
     * Marshaller
     */
    @Bean
    public McpMarshaller marshaller(McpOptions options) {
        return new McpMarshaller(options);
    }

    /**
     * Distributor
     */
    @Bean
    public McpCache cache() {
        return new McpCache();
    }

    /**
     * SnapshotBuilder
     */
    @Bean
    public SnapshotBuilder snapshotStore(ResourceDao resourceDao, McpMarshaller mcpMarshaller, McpOptions mcpOptions) {
        return new DBSnapshotBuilder(resourceDao, mcpMarshaller, mcpOptions);
    }

    /**
     * Status
     */
    @Bean
    public StatusProductor statusProductor(StatusDao statusDao) {
        return new StatusProductorImpl(statusDao);
    }


    /**
     * ConfigManager
     */
    @Bean
    public McpConfigStore configStore(ResourceDao resourceDao, GlobalConfig globalConfig) {
        return new McpConfigStore(resourceDao, globalConfig);
    }

    @Bean
    public GatewayConfigManager gatewayConfigManager(GatewayIstioModelEngine modelEngine, McpConfigStore k8sConfigStore, GlobalConfig globalConfig) {
        return new GatewayConfigManagerImpl(modelEngine, k8sConfigStore, globalConfig);
    }

    /**
     * Service
     */
    @Bean
    public GatewayService gatewayService(ResourceManager resourceManager, GatewayConfigManager configManager, StatusDao statusDao) {
        GatewayService innerService = new GatewayServiceImpl(resourceManager, configManager);
        return new McpGatewayService(innerService, statusDao);
    }
}
