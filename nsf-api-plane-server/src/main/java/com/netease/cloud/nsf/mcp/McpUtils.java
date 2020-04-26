package com.netease.cloud.nsf.mcp;

import istio.mcp.v1alpha1.Mcp;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/10
 **/
public class McpUtils {
    private static final String triggerCollection = "$triggerCollection";

    public static boolean isTriggerResponse(Mcp.RequestResources msg) {
        return triggerCollection.equals(msg.getCollection()) && msg.getErrorDetail() != null && msg.getErrorDetail().getCode() == 12;
    }

    public static boolean isSupportedCollection(Collection<String> supportedCollection, String collection) {
        if (Objects.isNull(supportedCollection)) return false;
        return supportedCollection.contains(collection);
    }

    public static String getResourceName(String namespace, String name) {
        if (StringUtils.isEmpty(namespace) && StringUtils.isEmpty(name)) {
            return "";
        }
        if (StringUtils.isEmpty(namespace)) {
            return name;
        }
        return namespace + "/" + name;
    }

    public static String getName(String resourceName) {
        int index = 0;
        if ((index = resourceName.indexOf("/")) != -1) {
            return resourceName.substring(index + 1);
        } else {
            return resourceName;
        }
    }

    public static String getNamespace(String resourceName) {
        int index = 0;
        if ((index = resourceName.indexOf("/")) != -1) {
            return resourceName.substring(0, index);
        } else {
            return null;
        }
    }
}
