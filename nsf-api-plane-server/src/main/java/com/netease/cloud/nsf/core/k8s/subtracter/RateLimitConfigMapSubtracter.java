package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.core.editor.PathExpressionEnum;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.function.Subtracter;
import io.fabric8.kubernetes.api.model.ConfigMap;
import me.snowdrop.istio.api.networking.v1alpha3.RateLimitConfig;

import java.util.Map;
import java.util.Optional;

public class RateLimitConfigMapSubtracter implements Subtracter<ConfigMap> {

    private String keyword;

    public RateLimitConfigMapSubtracter(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public ConfigMap subtract(ConfigMap configMap) {

        if (configMap == null || configMap.getData() == null) return configMap;

        Optional<Map.Entry<String, String>> firstEntry = configMap.getData().entrySet().stream().findFirst();
        if (!firstEntry.isPresent()) return configMap;
        RateLimitConfig rateLimitConfig = CommonUtil.yaml2Obj(firstEntry.get().getValue(), RateLimitConfig.class);
        ResourceGenerator gen = ResourceGenerator.newInstance(rateLimitConfig, ResourceType.OBJECT);
        gen.removeElement(PathExpressionEnum.REMOVE_RATELIMIT_CONFIGMAP_BY_API.translate(keyword));
        firstEntry.get().setValue(gen.yamlString());
        return configMap;
    }

}
