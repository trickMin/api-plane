package com.netease.cloud.nsf.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/4
 **/
public class CommonUtil {

    /**
     * match ip:port
     * 127.0.0.1:8080
     */
    private static final Pattern IP_PORT_PATTERN =
            Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):(6553[0-5]|655[0-2][0-9]|65[0-4][0-9][0-9]|6[0-4][0-9]{3}|[1-5][0-9]{4}|[2-9][0-9]{3}|1[1-9][0-9]{2}|10[3-9][0-9]|102[4-9])$");

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
}
