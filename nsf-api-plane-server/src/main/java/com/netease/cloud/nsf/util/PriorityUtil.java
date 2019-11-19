package com.netease.cloud.nsf.util;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.meta.API;
import com.netease.cloud.nsf.meta.UriMatch;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/14
 **/
public class PriorityUtil {

    //TODO 适配轻舟网关API

    private static final Map<UriMatch, Integer> matchScore =
            ImmutableMap.of(
                UriMatch.EXACT, 2,
                UriMatch.PREFIX, 1,
                UriMatch.REGEX, 0
            );

    public static int calculate(API api) {
        return calculate(api, Collections.emptyList());
    }

    /**
     *
     * @param api
     * @param paths override url
     * @return
     */
    public static int calculate(API api, List<String> paths) {

        List<String> urls = CollectionUtils.isEmpty(paths) ? api.getRequestUris() : paths;
        int urlLen = urls.stream().collect(Collectors.summingInt(url -> url.length()));
        int routeNum = routeNum(api);

        int priority = matchScore.get(api.getUriMatch())*20000 + urlLen*20 + routeNum;
        return priority;
    }

    private static int routeNum(API api) {
        int num = 0;
        if (!CollectionUtils.isEmpty(api.getMethods())) num++;
        if (!CollectionUtils.isEmpty(api.getHosts())) num++;
        return num;
    }
}
