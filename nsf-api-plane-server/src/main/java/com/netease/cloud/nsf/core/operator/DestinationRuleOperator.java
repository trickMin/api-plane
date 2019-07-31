package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.meta.ResourceEnum;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
public class DestinationRuleOperator implements IstioResourceOperator<DestinationRule> {

    @Override
    public DestinationRule merge(DestinationRule old, DestinationRule fresh) {

        return null;
    }

    @Override
    public boolean adapt(String name) {
        return ResourceEnum.DestinationRule.name().equals(name);
    }
}
