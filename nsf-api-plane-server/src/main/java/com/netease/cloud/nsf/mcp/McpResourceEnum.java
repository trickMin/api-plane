package com.netease.cloud.nsf.mcp;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import istio.networking.v1alpha3.*;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public enum McpResourceEnum {
    VirtualService("istio/networking/v1alpha3/virtualservices", VirtualServiceOuterClass.VirtualService.getDefaultInstance()),
    Gateway("istio/networking/v1alpha3/gateways", GatewayOuterClass.Gateway.getDefaultInstance()),
    DestinationRule("istio/networking/v1alpha3/destinationrules", DestinationRuleOuterClass.DestinationRule.getDefaultInstance()),
    GatewayPlugin("istio/networking/v1alpha3/gatewayplugins", GatewayPluginOuterClass.GatewayPlugin.getDefaultInstance()),
    PluginManager("istio/networking/v1alpha3/pluginmanagers", PluginManagerOuterClass.PluginManager.getDefaultInstance()),
    ServiceEntry("istio/networking/v1alpha3/serviceentries", ServiceEntryOuterClass.ServiceEntry.getDefaultInstance()),
    ConfigMap("api/v1/configmaps"),
    ;

    McpResourceEnum(String collection) {
        this.collection = collection;
        this.instance = Any.getDefaultInstance();
    }

    McpResourceEnum(String collection, Message instance) {
        this.collection = collection;
        this.instance = instance;
    }

    private String collection;
    private Message instance;

    public String getCollection() {
        return collection;
    }

    public Message getInstance() {
        return instance;
    }

    public static McpResourceEnum get(String name) {
        for (McpResourceEnum item : values()) {
            if (item.name().equalsIgnoreCase(name)) {
                return item;
            }
        }
        throw new ApiPlaneException(String.format("Unsupported mcp resource enum:[%s]", name));
    }

    public static McpResourceEnum getByCollection(String collection) {
        for (McpResourceEnum item : values()) {
            if (item.collection.equalsIgnoreCase(collection)) {
                return item;
            }
        }
        throw new ApiPlaneException(String.format("Unsupported mcp resource enum for collection:[%s]", collection));
    }
}
