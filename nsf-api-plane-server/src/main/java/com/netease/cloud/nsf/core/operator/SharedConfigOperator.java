package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/8/28
 **/
@Component
public class SharedConfigOperator implements IstioResourceOperator<SharedConfig> {

    @Override
    public SharedConfig merge(SharedConfig old, SharedConfig fresh) {
        SharedConfigSpec oldSpec = old.getSpec();
        SharedConfigSpec freshSpec = fresh.getSpec();

        List<RateLimitConfig> oldConfigs = oldSpec.getRateLimitConfigs();
        List<RateLimitConfig> freshConfigs = freshSpec.getRateLimitConfigs();

        if (CollectionUtils.isEmpty(oldConfigs)) {
            oldSpec.setRateLimitConfigs(freshConfigs);
            return old;
        }

        for (RateLimitConfig oldConfig : oldConfigs) {
            for (RateLimitConfig freshConfig : freshConfigs) {
                if (oldConfig.getDomain().equals(freshConfig.getDomain())) {
                    oldConfig.setDescriptors(mergeList(oldConfig.getDescriptors(), freshConfig.getDescriptors(), new RateLimitDescriptorEquals()));
                }
            }
        }

        oldSpec.setRateLimitConfigs(mergeList(freshConfigs, oldConfigs, new RateLimitConfigEquals()));

        return old;
    }

    private class RateLimitConfigEquals implements Equals<RateLimitConfig> {
        @Override
        public boolean apply(RateLimitConfig or, RateLimitConfig nr) {
            return Objects.equals(or.getDomain(), nr.getDomain());
        }
    }

    private class RateLimitDescriptorEquals implements Equals<RateLimitDescriptor> {
        @Override
        public boolean apply(RateLimitDescriptor or, RateLimitDescriptor nr) {
            return Objects.equals(or.getApi(), nr.getApi());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.SharedConfig.name().equals(name);
    }

    @Override
    public boolean isUseless(SharedConfig sharedConfig) {
        return sharedConfig == null ||
                sharedConfig.getSpec() == null ||
                  CollectionUtils.isEmpty(sharedConfig.getSpec().getRateLimitConfigs());
    }

    @Override
    public SharedConfig subtract(SharedConfig old, String service, String api) {
        ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT);
        gen.removeElement(PathExpressionEnum.REMOVE_SC_RATELIMITDESC.translate(buildDescriptorValue(service, api)));
        return gen.object(SharedConfig.class);
    }

    /**
     * 在DestinationRule的Subset中加了api属性，根据service+api生成api对应值
     * @param service
     * @param api
     * @return
     */
    public String buildDescriptorValue(String service, String api) {
        return String.format("%s-%s", service, api);
    }
}
