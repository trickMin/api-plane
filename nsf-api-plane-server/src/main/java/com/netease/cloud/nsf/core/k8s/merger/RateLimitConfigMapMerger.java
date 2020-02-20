package com.netease.cloud.nsf.core.k8s.merger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.netease.cloud.nsf.core.k8s.operator.SharedConfigOperator;
import com.netease.cloud.nsf.util.function.Merger;
import io.fabric8.kubernetes.api.model.ConfigMap;
import me.snowdrop.istio.api.networking.v1alpha3.RateLimitConfig;
import me.snowdrop.istio.api.networking.v1alpha3.SharedConfig;
import me.snowdrop.istio.api.networking.v1alpha3.SharedConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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

        ObjectMapper yamlMapper = getYamlMapper();
        Map.Entry<String, String> oldConfig = old.getData().entrySet().stream().findFirst().get();
        Map.Entry<String, String> latestConfig = latest.getData().entrySet().stream().findFirst().get();

        List<RateLimitConfig> oldRl = str2RateLimitConfigs(oldConfig.getValue(), yamlMapper);
        List<RateLimitConfig> latestRl = str2RateLimitConfigs(latestConfig.getValue(), yamlMapper);

        if (CollectionUtils.isEmpty(latestRl)) return old;
        if (CollectionUtils.isEmpty(oldRl)) return latest;

        SharedConfig oldSc = buildSharedConfig(oldRl);
        SharedConfig latestSc = buildSharedConfig(latestRl);

        SharedConfig mergedSc = new SharedConfigOperator().merge(oldSc, latestSc);
        String finalConfig = limitConfig2Str(mergedSc.getSpec().getRateLimitConfigs(), yamlMapper);
        if (!StringUtils.isEmpty(finalConfig)) {
            oldConfig.setValue(finalConfig);
        }
        return old;
    }

    private String limitConfig2Str(List<RateLimitConfig> rlcs, ObjectMapper om) {
        try {
            return om.writeValueAsString(rlcs);
        } catch (JsonProcessingException e) {
            logger.warn("write rate limit configs to string failed,", e);
        }
        return null;
    }

    private SharedConfig buildSharedConfig(List<RateLimitConfig> rateLimitConfigs) {
        SharedConfig sharedConfig = new SharedConfig();
        SharedConfigSpec spec = new SharedConfigSpec();
        spec.setRateLimitConfigs(rateLimitConfigs);
        sharedConfig.setSpec(spec);
        return sharedConfig;
    }

    private List<RateLimitConfig> str2RateLimitConfigs(String str, ObjectMapper om) {
        try {
            return Arrays.asList(om.readValue(str, RateLimitConfig[].class));
        } catch (IOException e) {
            logger.warn("translate str {} to rate limit config failed,", str, e);
        }
        return Collections.EMPTY_LIST;
    }

    private ObjectMapper getYamlMapper() {
        YAMLMapper yamlMapper = new YAMLMapper();
        // 不输出---
        yamlMapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
        // 不输出引号
        yamlMapper.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return yamlMapper;
    }
}
