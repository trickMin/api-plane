package com.netease.cloud.nsf.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.util.function.Equals;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/4
 **/
public class CommonUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    private static YAMLMapper yamlMapper;
    /**
     * match ip:port
     * 127.0.0.1:8080
     */
    private static final Pattern IP_PORT_PATTERN =
            Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):(6553[0-5]|655[0-2][0-9]|65[0-4][0-9][0-9]|6[0-4][0-9]{3}|[1-5][0-9]{4}|[0-9]{1,4})$");

    public static Map<String, String> str2Label(String str) {

        Map<String, String> labelMap = new HashMap<>();
        if (StringUtils.isEmpty(str) || !str.contains(":")) return labelMap;
        String[] label = str.split(":");
        labelMap.put(label[0], label[1]);
        return labelMap;
    }

    public static Map<String, String> strs2Label(List<String> strs) {

        Map<String, String> labelMap = new HashMap<>();
        if (CollectionUtils.isEmpty(strs)) return labelMap;
        strs.forEach(s -> {
            labelMap.putAll(str2Label(s));
        });
        return labelMap;
    }

    /**
     *
     * @param ipAddr ip:port
     * @return
     */
    public static boolean isValidIPPortAddr(String ipAddr) {
        return IP_PORT_PATTERN.matcher(ipAddr).matches();
    }

    /**
     * host变为正则
     *
     * . -> \.
     * * -> .+
     *
     * *.163.com -> .+\.163\.com
     * @return
     */
    public static String host2Regex(String host) {
        if (host.equals("*")) return ".*";
        return host.replace(".", "\\.")
                    .replace("*", ".+");
    }

    public static String obj2yaml(Object o) {

        if (o == null) return null;
        try {
            return getYamlMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            logger.warn("obj {} to yaml failed", o, e);
        }
        return null;
    }

    public static <T> T yaml2Obj(String yaml, Class<T> clazz) {

        if (StringUtils.isEmpty(yaml)) return null;
        try {
            return getYamlMapper().readValue(yaml, clazz);
        } catch (IOException e) {
            logger.warn("yaml {} to obj failed,", yaml);
        }
        return null;
    }

    private static YAMLMapper getYamlMapper() {
        if (yamlMapper == null) {
            YAMLMapper mapper = new YAMLMapper();
            mapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
            mapper.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            yamlMapper = mapper;
        }
        return yamlMapper;
    }

    /**
     * 合并两个list, 当遇到相等的两个element时，
     * 用新list中的element取代老list中的element，
     * 不相等的element全部保留。
     * @param oldL
     * @param newL
     * @param eq
     * @return
     */
     public static List mergeList(List oldL, List newL, Equals eq) {
        List result = null;
        if (!CollectionUtils.isEmpty(newL)) {
            if (CollectionUtils.isEmpty(oldL)) {
                return newL;
            } else {
                result = new ArrayList(oldL);
                for (Object no : newL) {
                    for (Object oo : oldL) {
                        if (eq.apply(no, oo)) {
                            result.remove(oo);
                        }
                    }
                }
                result.addAll(newL);
            }
        }
        return result;
    }

    public static List dropList(List oldL, Object identical, Equals eq) {
        if (CollectionUtils.isEmpty(oldL)) return oldL;
        List result = new ArrayList(oldL);
        for (Object oldO : oldL) {
            if (eq.apply(oldO, identical)) {
                result.remove(oldO);
            }
        }
        return result;
    }

    public static HasMetadata json2HasMetadata(String json) {
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(json, ResourceType.JSON);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        return gen.object(resourceEnum.mappingType());
    }

    public static boolean isLuaPlugin(String plugin) {
        ResourceGenerator source = ResourceGenerator.newInstance(plugin);
        String type = source.getValue("$.type");
        String kind = source.getValue("$.kind");
        return "lua".equals(type) || "trace".equals(kind);
    }
}
