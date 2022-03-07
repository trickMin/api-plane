package com.netease.cloud.nsf.core.k8s.operator;

import com.netease.cloud.nsf.core.editor.PathExpressionEnum;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.proto.k8s.K8sTypes;
import com.netease.cloud.nsf.util.function.Equals;
import istio.networking.v1alpha3.DestinationRuleOuterClass;
import me.snowdrop.istio.api.networking.v1alpha3.Subset;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/30
 **/
@Component
public class DestinationRuleOperator implements k8sResourceOperator<K8sTypes.DestinationRule> {

    @Override
    public K8sTypes.DestinationRule merge(K8sTypes.DestinationRule old, K8sTypes.DestinationRule fresh) {
        DestinationRuleOuterClass.DestinationRule oldSpec = old.getSpec();
        DestinationRuleOuterClass.DestinationRule freshSpec = fresh.getSpec();

        K8sTypes.DestinationRule latest = new K8sTypes.DestinationRule();
        latest.setKind(old.getKind());
        latest.setApiVersion(old.getApiVersion());
        latest.setMetadata(old.getMetadata());
        latest.setSpec(DestinationRuleOuterClass.DestinationRule.newBuilder()
                .setAltStatName(freshSpec.getAltStatName())
                .setHost(freshSpec.getHost())
                .setTrafficPolicy(freshSpec.getTrafficPolicy())
                .addAllSubsets(mergeList(oldSpec.getSubsetsList(), freshSpec.getSubsetsList(), new SubsetEquals())).build());
        return latest;

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

    @Override
    public boolean isUseless(K8sTypes.DestinationRule destinationRule) {
        return destinationRule == null ||
                StringUtils.isEmpty(destinationRule.getApiVersion()) ||
                 destinationRule.getSpec() == null ||
                  CollectionUtils.isEmpty(destinationRule.getSpec().getSubsetsList());
    }

    @Override
    public K8sTypes.DestinationRule subtract(K8sTypes.DestinationRule old, String value) {
        ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT);
        gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET_API.translate(value));
        return gen.object(K8sTypes.DestinationRule.class);
    }

    /**
     * 在DestinationRule的Subset中加了api属性，根据service+api生成api对应值
     * @param service
     * @param api
     * @return
     */
    public String buildSubsetApi(String service, String api) {
        return String.format("%s-%s", service, api);
    }
}
