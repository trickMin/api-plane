package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.util.function.Subtracter;
import com.netease.slime.api.microservice.v1alpha1.RateLimitDescriptorConfigSpec;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/10
 **/
public class SmartLimiterSubtracter implements Subtracter<SmartLimiter> {

    private String host;
    private Long ruleId;

    public SmartLimiterSubtracter(String host, Long ruleId) {
        this.host = host;
        this.ruleId = ruleId;
    }

    @Override
    public SmartLimiter subtract(SmartLimiter smartLimiter) {

        if (smartLimiter == null ||
                smartLimiter.getSpec() == null ||
                smartLimiter.getSpec().getRatelimitConfig() == null ||
                CollectionUtils.isEmpty(smartLimiter.getSpec().getRatelimitConfig().getDescriptors())) return smartLimiter;

        Pattern pattern = Pattern.compile(String.format("^Service\\[%s\\]-.*-Api\\[%s\\].+",host, ruleId+""));

        List<RateLimitDescriptorConfigSpec> descriptors = smartLimiter.getSpec().getRatelimitConfig().getDescriptors().stream()
                .filter(desc -> !pattern.matcher(desc.getDescriptors().get(0).getValue()).find())
                .collect(Collectors.toList());

        smartLimiter.getSpec().getRatelimitConfig().setDescriptors(descriptors);
        return smartLimiter;
    }
}
