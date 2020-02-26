package com.netease.cloud.nsf.core.k8s.merger;

import com.netease.cloud.nsf.meta.ConfigMapRateLimit;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.function.Equals;
import com.netease.cloud.nsf.util.function.Merger;
import io.fabric8.kubernetes.api.model.ConfigMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * for ratelimit server config map
 */
public class RateLimitConfigMapMerger implements Merger<ConfigMap> {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitConfigMapMerger.class);

    @Override
    public ConfigMap merge(ConfigMap old, ConfigMap latest) {

        if (latest == null || CollectionUtils.isEmpty(latest.getData())) return old;
        if (old == null || CollectionUtils.isEmpty(old.getData())) return latest;

        Map.Entry<String, String> oldConfig = old.getData().entrySet().stream().findFirst().get();
        Map.Entry<String, String> latestConfig = latest.getData().entrySet().stream().findFirst().get();

        // k8s上的非数组，本地渲染的为数组
        ConfigMapRateLimit oldCmrl = str2RateLimitConfig(oldConfig.getValue());
        ConfigMapRateLimit latestCmrl = str2RateLimitConfig(latestConfig.getValue());

        if (oldCmrl == null) return old;
        if (latestCmrl == null) return latest;

        List mergedDescriptors = CommonUtil.mergeList(
                oldCmrl.getDescriptors(), latestCmrl.getDescriptors(), new RateLimitDescriptorEquals());

        //仅对descriptors进行覆盖
        oldCmrl.setDescriptors(mergedDescriptors);

        String finalConfig = limitConfig2Str(oldCmrl);
        if (!StringUtils.isEmpty(finalConfig)) {
            oldConfig.setValue(finalConfig);
        }
        return old;
    }


    private class RateLimitDescriptorEquals implements Equals<ConfigMapRateLimit.ConfigMapRateLimitDescriptor> {
        @Override
        public boolean apply(ConfigMapRateLimit.ConfigMapRateLimitDescriptor or, ConfigMapRateLimit.ConfigMapRateLimitDescriptor nr) {

            String oldVal = or.getValue();
            String newVal = nr.getValue();

            //eg. Service[httpbin]-User[none]-Api[httpbin]-Id[08638e47-48db-43bc-9c21-07ef892b5494]
            // 当Api[]中的值相等时，才认为两者相当
            Pattern pattern = Pattern.compile("(Service.*)-(User.*)-(Api.*)-(Id.*)");
            Matcher oldMatcher = pattern.matcher(oldVal);
            Matcher newMatcher = pattern.matcher(newVal);
            if (oldMatcher.find() && newMatcher.find()) {
                return Objects.equals(oldMatcher.group(3), newMatcher.group(3));
            }
            return false;
        }
    }

    private ConfigMapRateLimit buildConfigMapRateLimit(String domain,
                                                       List<ConfigMapRateLimit.ConfigMapRateLimitDescriptor> descriptors) {
        ConfigMapRateLimit cmrl = new ConfigMapRateLimit();
        cmrl.setDomain(domain);
        cmrl.setDescriptors(descriptors);
        return cmrl;
    }

    private String limitConfig2Str(ConfigMapRateLimit cmrl) {
        return CommonUtil.obj2yaml(cmrl);
    }


    private ConfigMapRateLimit str2RateLimitConfig(String str) {
        return CommonUtil.yaml2Obj(str, ConfigMapRateLimit.class);
    }
}
