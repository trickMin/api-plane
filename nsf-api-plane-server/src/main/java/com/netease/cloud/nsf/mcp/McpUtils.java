package com.netease.cloud.nsf.mcp;

import com.google.common.collect.ImmutableMap;
import istio.mcp.v1alpha1.Mcp;
import org.apache.commons.collections.MapUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
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

    public static String getLabel(Map<String, String> label) {
        if (MapUtils.isEmpty(label)) label = ImmutableMap.of();
        Map.Entry<String, String>[] entries = sortMap(label);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, String> entry : entries) {
            sb.append(String.format("[%s,%s]", entry.getKey(), entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    public static String getLabelMatch(Map<String, String> label) {
        if (MapUtils.isEmpty(label)) label = ImmutableMap.of();
        Map.Entry<String, String>[] entries = sortMap(label);
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, String> entry : entries) {
            sb.append("%").append(String.format("[%s,%s]", entry.getKey(), entry.getValue()));
        }
        sb.append("%");
        sb.append("}");
        return sb.toString();
    }

    private static Map.Entry<String, String>[] sortMap(Map<String, String> map) {
        Map.Entry<String, String>[] entries = map.entrySet().toArray(new Map.Entry[0]);
        Arrays.sort(entries, Map.Entry.comparingByKey());
        return entries;
    }

}
