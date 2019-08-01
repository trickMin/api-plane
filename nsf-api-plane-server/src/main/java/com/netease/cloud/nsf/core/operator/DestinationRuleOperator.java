package com.netease.cloud.nsf.core.operator;

import com.netease.cloud.nsf.meta.K8sResourceEnum;
import com.netease.cloud.nsf.util.function.Equals;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRuleSpec;
import me.snowdrop.istio.api.networking.v1alpha3.Subset;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
public class DestinationRuleOperator implements IstioResourceOperator<DestinationRule> {

    @Override
    public DestinationRule merge(DestinationRule old, DestinationRule fresh) {

        DestinationRuleSpec oldSpec = old.getSpec();
        DestinationRuleSpec freshSpec = fresh.getSpec();

        List<Subset> filteredSubsets = filterSameSubset(oldSpec, freshSpec);
        oldSpec.setSubsets(mergeList(filteredSubsets, freshSpec.getSubsets(), new SubsetEquals()));
        return old;
    }

    private List<Subset> filterSameSubset(DestinationRuleSpec oldSpec, DestinationRuleSpec freshSpec) {

        if (CollectionUtils.isEmpty(oldSpec.getSubsets())) return freshSpec.getSubsets();
        if (CollectionUtils.isEmpty(freshSpec.getSubsets())) return oldSpec.getSubsets();

        return oldSpec.getSubsets().stream()
                .filter(os -> {
                    for (Subset fs : freshSpec.getSubsets()) {
                        if (Objects.equals(fs.getName(), os.getName())) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
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
