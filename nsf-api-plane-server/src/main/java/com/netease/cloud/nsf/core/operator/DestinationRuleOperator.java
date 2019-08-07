package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRuleSpec;
import me.snowdrop.istio.api.networking.v1alpha3.Subset;
import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
public class DestinationRuleOperator implements IstioResourceOperator<DestinationRule> {

    @Override
    public DestinationRule merge(DestinationRule old, DestinationRule fresh) {

        DestinationRuleSpec oldSpec = old.getSpec();
        DestinationRuleSpec freshSpec = fresh.getSpec();

        oldSpec.setSubsets(mergeList(oldSpec.getSubsets(), freshSpec.getSubsets(), new SubsetEquals()));
        return old;
    }

    private class SubsetEquals implements Equals<Subset> {

        @Override
        public boolean apply(Subset ot, Subset nt) {
            return Objects.equals(ot.getName(), nt.getName());
        }
    }

    @Override
    public boolean adapt(String name) {
        return K8sResourceEnum.DestinationRule.name().equals(name);
    }
}
