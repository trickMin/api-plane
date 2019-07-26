package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.meta.K8sResourceEnum;
import me.snowdrop.istio.api.rbac.v1alpha1.ServiceRoleBinding;
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
public class ServiceRoleBindingClient extends AbstractK8sClient<ServiceRoleBinding> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRoleBindingClient.class);

    @Override
    public void createOrUpdate(List<ServiceRoleBinding> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            logger.warn("createOrUpdate failed. empty resources list");
            return;
        }
        resources.stream().forEach(r -> createOrUpdate(r));
    }

    @Override
    public void createOrUpdate(ServiceRoleBinding resource) {
        istioClient().serviceRoleBinding()
                .inNamespace(resource.getMetadata().getNamespace())
                .createOrReplace(resource);
    }

    @Override
    public void delete(String kind, String name, String namespace) {
        istioClient().serviceRoleBinding()
                .inNamespace(name).withName(name).delete();
    }

    @Override
    public List<ServiceRoleBinding> getList(String kind, String namespace) {
        return istioClient().serviceRoleBinding().inNamespace(namespace).list().getItems();
    }

    @Override
    public ServiceRoleBinding get(String kind, String name, String namespace) {
        return istioClient().serviceRoleBinding().inNamespace(namespace).withName(name).get();
    }

    @Override
    public boolean isAdapt(String kind) {
        return K8sResourceEnum.ServiceRoleBinding.equals(K8sResourceEnum.get(kind));
    }
}
