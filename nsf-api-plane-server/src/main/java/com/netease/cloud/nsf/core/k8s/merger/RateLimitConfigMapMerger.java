package com.netease.cloud.nsf.core.k8s.merger;

import com.netease.cloud.nsf.core.k8s.operator.SharedConfigOperator;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.function.Merger;
import io.fabric8.kubernetes.api.model.ConfigMap;
import me.snowdrop.istio.api.networking.v1alpha3.RateLimitConfig;
import me.snowdrop.istio.api.networking.v1alpha3.SharedConfig;
import me.snowdrop.istio.api.networking.v1alpha3.SharedConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


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
        RateLimitConfig oldRl = str2RateLimitConfig(oldConfig.getValue());
        RateLimitConfig latestRl = str2RateLimitConfig(latestConfig.getValue());

        if (latestRl == null) return old;
        if (oldRl == null) return latest;

        SharedConfig oldSc = buildSharedConfig(Arrays.asList(oldRl));
        SharedConfig latestSc = buildSharedConfig(Arrays.asList(latestRl));

        SharedConfig mergedSc = new SharedConfigOperator().merge(oldSc, latestSc);
        String finalConfig = limitConfig2Str(mergedSc.getSpec().getRateLimitConfigs().get(0));
        if (!StringUtils.isEmpty(finalConfig)) {
            oldConfig.setValue(finalConfig);
        }
        return old;
    }

    private String limitConfig2Str(RateLimitConfig rlc) {
        return CommonUtil.obj2yaml(rlc);
    }

    private SharedConfig buildSharedConfig(List<RateLimitConfig> rateLimitConfigs) {
        SharedConfig sharedConfig = new SharedConfig();
        SharedConfigSpec spec = new SharedConfigSpec();
        spec.setRateLimitConfigs(rateLimitConfigs);
        sharedConfig.setSpec(spec);
        return sharedConfig;
    }

    private RateLimitConfig str2RateLimitConfig(String str) {
        return CommonUtil.yaml2Obj(str, RateLimitConfig.class);
    }
}
