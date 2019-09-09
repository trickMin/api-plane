package com.netease.cloud.nsf.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/4
 **/
public class CommonUtil {

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

}
