package com.netease.cloud.nsf.meta;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/16
 **/
public enum UriMatch {

    EXACT,
    PREFIX,
    REGEX,
    ;

    public static UriMatch get(String type) {
        if (StringUtils.isEmpty(type)) throw new IllegalArgumentException();
        for (UriMatch uriMatch : values()) {
            if (uriMatch.name().equalsIgnoreCase(type)) {
                return uriMatch;
            }
        }
        throw new NullPointerException();
    }
}
