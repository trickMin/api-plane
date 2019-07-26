package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.meta.K8sResourceEnum;
import me.snowdrop.istio.api.authentication.v1alpha1.Policy;
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
public class PolicyClient extends AbstractK8sClient<Policy> {
    private static final Logger logger = LoggerFactory.getLogger(PolicyClient.class);

    @Override
    public void createOrUpdate(List<Policy> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            logger.warn("createOrUpdate failed. empty resources list");
            return;
        }
        resources.stream().forEach(r -> createOrUpdate(r));
    }

    @Override
    public void createOrUpdate(Policy resource) {
        istioClient().policy()
                .inNamespace(resource.getMetadata().getNamespace())
                .createOrReplace(resource);
    }

    @Override
    public void delete(String kind, String name, String namespace) {
        istioClient().policy()
                .inNamespace(name).withName(name).delete();
    }

    @Override
    public List<Policy> getList(String kind, String namespace) {
        return istioClient().policy().inNamespace(namespace).list().getItems();
    }

    @Override
    public Policy get(String kind, String name, String namespace) {
        return istioClient().policy().inNamespace(namespace).withName(name).get();
    }

    @Override
    public boolean isAdapt(String kind) {
        return K8sResourceEnum.Policy.equals(K8sResourceEnum.get(kind));
    }
}
