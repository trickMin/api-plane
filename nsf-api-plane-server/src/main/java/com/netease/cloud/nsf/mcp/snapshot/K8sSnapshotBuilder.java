package com.netease.cloud.nsf.mcp.snapshot;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.google.protobuf.util.Timestamps;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.mcp.McpUtils;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import istio.mcp.nsf.SnapshotOuterClass;
import istio.mcp.v1alpha1.Mcp;
import istio.mcp.v1alpha1.MetadataOuterClass;
import istio.mcp.v1alpha1.ResourceOuterClass;
import istio.networking.v1alpha3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/15
 **/
public class K8sSnapshotBuilder implements SnapshotBuilder{
    private final KubernetesClient kubernetesClient;

    private static final Logger logger = LoggerFactory.getLogger(K8sSnapshotBuilder.class);

    private static final String namespace = "gateway-system";

    @Autowired
    public K8sSnapshotBuilder(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public SnapshotOuterClass.Snapshot build() {
        SnapshotOuterClass.Snapshot.Builder builder = SnapshotOuterClass.Snapshot.newBuilder();
        MethodDescriptor.Marshaller<Message> marshaller;
        String collection;

        marshaller = ProtoUtils.jsonMarshaller(VirtualServiceOuterClass.VirtualService.getDefaultInstance());
        collection = "istio/networking/v1alpha3/virtualservices";
        builder.putResources(collection, Mcp.Resources.newBuilder()
                .setCollection(collection)
                .addAllResources(getResources("VirtualService", namespace, marshaller))
                .setSystemVersionInfo("")
                .build());

        marshaller = ProtoUtils.jsonMarshaller(GatewayOuterClass.Gateway.getDefaultInstance());
        builder.putResources(collection, Mcp.Resources.newBuilder()
                .setCollection(collection)
                .addAllResources(getResources("Gateway", namespace, marshaller))
                .setSystemVersionInfo("")
                .build());

        marshaller = ProtoUtils.jsonMarshaller(DestinationRuleOuterClass.DestinationRule.getDefaultInstance());
        builder.putResources(collection, Mcp.Resources.newBuilder()
                .setCollection(collection)
                .addAllResources(getResources("DestinationRule", namespace, marshaller))
                .setSystemVersionInfo("")
                .build());

        marshaller = ProtoUtils.jsonMarshaller(PluginManagerOuterClass.PluginManager.getDefaultInstance());
        builder.putResources(collection, Mcp.Resources.newBuilder()
                .setCollection(collection)
                .addAllResources(getResources("PluginManager", namespace, marshaller))
                .setSystemVersionInfo("")
                .build());

        marshaller = ProtoUtils.jsonMarshaller(GatewayPluginOuterClass.GatewayPlugin.getDefaultInstance());
        builder.putResources(collection, Mcp.Resources.newBuilder()
                .setCollection(collection)
                .addAllResources(getResources("GatewayPlugin", namespace, marshaller))
                .setSystemVersionInfo("")
                .build());

        builder.setVersion(new Date().toString());
        return builder.build();
    }


    public List<ResourceOuterClass.Resource> getResources(String kind,
                                                          String namespace,
                                                          MethodDescriptor.Marshaller<Message> marshaller) {
        String url = kubernetesClient.getUrl(kind, namespace);
        String json = kubernetesClient.get(url);
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(json);
        List<String> items = gen.items();
        List<ResourceOuterClass.Resource> ret = new ArrayList<>();
        for (String item : items) {
            K8sResourceGenerator itemGen = K8sResourceGenerator.newInstance(item);
            Object spec = itemGen.getSpec();
            String resourceName = McpUtils.getResourceName(itemGen.getNamespace(), itemGen.getName());
            String version = itemGen.getResourceVersion();
            String createTime = itemGen.getCreateTimestamp();
            if (Objects.isNull(spec)) {
                logger.warn("Resource :{} has not spec", resourceName);
                continue;
            }
            ret.add(getResource(
                    ResourceGenerator.obj2json(spec),
                    resourceName,
                    version,
                    createTime,
                    itemGen.getLabels(),
                    itemGen.getAnnotations(),
                    marshaller));
        }
        return ret;
    }

    public ResourceOuterClass.Resource getResource(String json,
                                                   String name,
                                                   String version,
                                                   String createTime,
                                                   Map<String, String> labels,
                                                   Map<String, String> annotations,
                                                   MethodDescriptor.Marshaller<Message> marshaller) {
        MetadataOuterClass.Metadata.Builder metadata = MetadataOuterClass.Metadata
                .newBuilder()
                .setName(name)
                .setVersion(version);

        try {
            metadata.setCreateTime(Timestamps.parse(createTime));
        } catch (ParseException e) {
            logger.warn("parse timestamp {} error:{}", createTime, e);
        }
        if (Objects.nonNull(labels)) {
            metadata.putAllLabels(labels);
        }
        if (Objects.nonNull(annotations)) {
            metadata.putAllAnnotations(annotations);
        }

        try {
            Message message = marshaller.parse(getInputStream(json));
            return ResourceOuterClass.Resource
                    .newBuilder()
                    .setMetadata(metadata)
                    .setBody(Any.pack(message))
                    .build();
        } catch (Exception e) {
            logger.warn("Marshal json to proto failure. config:{}, json:{}", name, json);
            throw e;
        }
    }


    public InputStream getInputStream(String json) {
        return new ByteArrayInputStream(json.getBytes());
    }
}
