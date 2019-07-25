package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.meta.K8sResourceEnum;
import me.snowdrop.istio.api.networking.v1alpha3.DestinationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
@Order(0)
@Component
public class DestinationRuleClient extends AbstractK8sClient<DestinationRule> {
    private static final Logger logger = LoggerFactory.getLogger(DestinationRule.class);

    @Override
    public void createOrUpdate(List<DestinationRule> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            logger.warn("createOrUpdate failed. empty resources list");
            return;
        }
        resources.stream().forEach(r -> createOrUpdate(r));
    }

    @Override
    public void createOrUpdate(DestinationRule resource) {
        istioClient().destinationRule()
                .inNamespace(resource.getMetadata().getNamespace())
                .createOrReplace(resource);
    }

    @Override
    public void delete(String kind, String name, String namespace) {
        istioClient().destinationRule()
                .inNamespace(name).withName(name).delete();
    }

    @Override
    public List<DestinationRule> getList(String kind, String namespace) {
        return istioClient().destinationRule().inNamespace(namespace).list().getItems();
    }

    @Override
    public DestinationRule get(String kind, String name, String namespace) {
        return istioClient().destinationRule().inNamespace(namespace).withName(name).get();
    }

    @Override
    public boolean isAdapt(String kind) {
        return K8sResourceEnum.DestinationRule.equals(K8sResourceEnum.get(kind));
    }
}
