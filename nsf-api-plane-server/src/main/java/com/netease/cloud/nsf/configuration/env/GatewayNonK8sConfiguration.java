package com.netease.cloud.nsf.configuration.env;

import com.netease.cloud.nsf.configuration.GatewayAutoConfiguration;
import com.netease.cloud.nsf.core.GlobalConfig;
import com.netease.cloud.nsf.core.gateway.GatewayIstioModelEngine;
import com.netease.cloud.nsf.core.gateway.service.GatewayConfigManager;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.core.gateway.service.impl.GatewayConfigManagerImpl;
import com.netease.cloud.nsf.mcp.*;
import com.netease.cloud.nsf.mcp.aop.ConfigStoreAop;
import com.netease.cloud.nsf.mcp.aop.GatewayServiceAop;
import com.netease.cloud.nsf.mcp.dao.ResourceDao;
import com.netease.cloud.nsf.mcp.dao.StatusDao;
import com.netease.cloud.nsf.mcp.dao.impl.ResourceDaoImpl;
import com.netease.cloud.nsf.mcp.dao.impl.StatusDaoImpl;
import com.netease.cloud.nsf.mcp.ratelimit.RlsClusterClient;
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
import org.apache.commons.lang3.StringUtils;
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
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/10
 **/
@Configuration
@ConditionalOnProperty("nonK8sMode")
public class GatewayNonK8sConfiguration {
    @Value("${mcpPort:8899}")
    private Integer port;

    @Value("${rlsAddresses:#{null}}")
    private String rlsAddresses;

    @Bean
    public McpOptions options() {
        McpOptions options = new McpOptions();
        options.setStatusCheckIntervalMs(1000L);

        options.registerSnapshotCollection(McpResourceEnum.VirtualService.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.Gateway.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.DestinationRule.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.GatewayPlugin.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.PluginManager.getCollection());
        options.registerSnapshotCollection(McpResourceEnum.ServiceEntry.getCollection());

        options.registerDescriptor(SnapshotOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(ResourceOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(VirtualServiceOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(DestinationRuleOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(GatewayOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(PluginManagerOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(GatewayPluginOuterClass.getDescriptor().getMessageTypes());
        options.registerDescriptor(ServiceEntryOuterClass.getDescriptor().getMessageTypes());

        if (!StringUtils.isEmpty(rlsAddresses)) {
            String[] addresses = StringUtils.split(rlsAddresses, ",");
            for (String address : addresses) {
                options.registerRls(address);
            }
        }
        return options;
    }

    /**
     * Monitor
     */
    @Bean
    public StatusMonitor monitor(McpOptions options, StatusProductor productor, SnapshotBuilder builder, McpResourceDistributor distributor, TransactionTemplate transactionTemplate, RlsClusterClient rlsClusterClient) {
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
                        logger.info("MCP: SnapshotBuilder: build snapshot for version:[{}], consume:[{}]", snapshot.getVersion(), System.currentTimeMillis() - start + "ms");
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
        monitor.registerHandler(StatusConst.RATELIMIT_VERSION, (((event, property) -> {
            rlsClusterClient.sync();
        })));
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

    @Bean
    public StatusNotifier statusNotifier(StatusDao statusDao) {
        return new StatusNotifierImpl(statusDao, key -> new Date().toString());
    }

    /**
     * Ratelimit Server Client
     */
    @Bean
    public RlsClusterClient rlsClusterClient(McpOptions mcpOptions, ResourceDao resourceDao, McpMarshaller marshaller) {
        return new RlsClusterClient(mcpOptions, resourceDao, marshaller);
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
    public GatewayService gatewayService(ResourceManager resourceManager, GatewayConfigManager configManager) {
        return new GatewayServiceImpl(resourceManager, configManager);
    }

    /**
     * AOP
     * 1. 为GatewayService开启事务
     * 2. 执行特定方法后更新Status表
     */
    @Bean
    public GatewayServiceAop gatewayServiceAop(TransactionTemplate transactionTemplate, StatusNotifier statusNotifier) {
        return new GatewayServiceAop(transactionTemplate, statusNotifier);
    }

    @Bean
    public ConfigStoreAop configStoreAop(TransactionTemplate transactionTemplate, StatusNotifier statusNotifier) {
        return new ConfigStoreAop(transactionTemplate, statusNotifier);
    }
}
