package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.meta.ConfigMapRateLimit;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.function.Subtracter;
import io.fabric8.kubernetes.api.model.ConfigMap;

import java.util.Map;
import java.util.Optional;

public abstract class RateLimitConfigMapSubtracter implements Subtracter<ConfigMap> {

    @Override
    public ConfigMap subtract(ConfigMap configMap) {

        if (configMap == null || configMap.getData() == null) return configMap;

        Optional<Map.Entry<String, String>> firstEntry = configMap.getData().entrySet().stream().findFirst();
        if (!firstEntry.isPresent()) return configMap;
        ConfigMapRateLimit rateLimitConfig = CommonUtil.yaml2Obj(firstEntry.get().getValue(), ConfigMapRateLimit.class);
        ResourceGenerator gen = ResourceGenerator.newInstance(rateLimitConfig, ResourceType.OBJECT);
        gen.removeElement(getPath());
        firstEntry.get().setValue(gen.yamlString());
        return configMap;
    }

    public abstract String getPath();
}
