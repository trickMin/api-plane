package com.netease.cloud.nsf.core.k8s.merger;

import com.netease.cloud.nsf.meta.ConfigMapRateLimit;
import com.netease.cloud.nsf.util.function.Equals;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/5/12
 **/
public class MeshRateLimitConfigMapMerger extends RateLimitConfigMapMerger {

    @Override
    Equals<ConfigMapRateLimit.ConfigMapRateLimitDescriptor> getDescriptorEquals() {

        return (ot, nt) -> {
            String oldVal = ot.getValue();
            String newVal = nt.getValue();

            //eg. Service[httpbin]-User[none]-Gateway[gw]-Api[httpbin]-Id[08638e47-48db-43bc-9c21-07ef892b5494]
            // 当Service[]的值相等时，认为两者相当
            Pattern pattern = Pattern.compile("(Service.*)-(User.*)-(Gateway.*)-(Api.*)-(Id.*)");
            Matcher oldMatcher = pattern.matcher(oldVal);
            Matcher newMatcher = pattern.matcher(newVal);
            if (oldMatcher.find() && newMatcher.find()) {
                return Objects.equals(oldMatcher.group(1), newMatcher.group(1));
            }
            return false;
        };
    }
}
