package com.netease.cloud.nsf.core.k8s.subtracter;

import com.netease.cloud.nsf.core.editor.PathExpressionEnum;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.proto.k8s.K8sTypes;
import com.netease.cloud.nsf.util.function.Subtracter;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/17
 **/
public class GatewayDestinationRuleSubtracter implements Subtracter<K8sTypes.DestinationRule> {

    private String key;

    public GatewayDestinationRuleSubtracter(String key) {
        this.key = key;
    }

    @Override
    public K8sTypes.DestinationRule subtract(K8sTypes.DestinationRule old) {
        ResourceGenerator gen = ResourceGenerator.newInstance(old, ResourceType.OBJECT);
        gen.removeElement(PathExpressionEnum.REMOVE_DST_SUBSET_API.translate(key));
        return gen.object(K8sTypes.DestinationRule.class);
    }
}
