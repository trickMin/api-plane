package com.netease.cloud.nsf.core.k8s.merger;

import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.function.Equals;
import com.netease.cloud.nsf.util.function.Merger;
import com.netease.slime.api.microservice.v1alpha1.RateLimitDescriptorConfigSpec;
import com.netease.slime.api.microservice.v1alpha1.SmartLimiter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/10
 **/
public class SmartLimiterMerger implements Merger<SmartLimiter> {

    @Override
    public SmartLimiter merge(SmartLimiter old, SmartLimiter latest) {

        if (isNull(latest)) return old;
        if (isNull(old)) return latest;

        List<RateLimitDescriptorConfigSpec> oldDescriptor = old.getSpec().getRatelimitConfig().getDescriptors();
        List<RateLimitDescriptorConfigSpec> latestDescriptor = latest.getSpec().getRatelimitConfig().getDescriptors();

        List newDescriptor = CommonUtil.mergeList(oldDescriptor, latestDescriptor, new RateLimitDescriptorConfigEquals());
        old.getSpec().getRatelimitConfig().setDescriptors(newDescriptor);
        return old;
    }

    private class RateLimitDescriptorConfigEquals implements Equals<RateLimitDescriptorConfigSpec> {

        @Override
        public boolean apply(RateLimitDescriptorConfigSpec ot, RateLimitDescriptorConfigSpec nt) {

            String key1 = ot.getDescriptors().get(0).getValue();
            String key2 = nt.getDescriptors().get(0).getValue();

            Pattern pattern = Pattern.compile("(Service.*)-(User.*)-(Gateway.*)-(Api.*)-(Id.*)");
            Matcher oldMatcher = pattern.matcher(key1);
            Matcher newMatcher = pattern.matcher(key2);

            if (oldMatcher.find() && newMatcher.find()) {
                return Objects.equals(oldMatcher.group(1), newMatcher.group(1)) &&
                        Objects.equals(oldMatcher.group(4), newMatcher.group(4));
            }
            return false;
        }
    }

    private boolean isNull(SmartLimiter sl) {

        return sl == null ||
                sl.getSpec() == null ||
                sl.getSpec().getRatelimitConfig() == null ||
                CollectionUtils.isEmpty(sl.getSpec().getRatelimitConfig().getDescriptors());
    }

}
