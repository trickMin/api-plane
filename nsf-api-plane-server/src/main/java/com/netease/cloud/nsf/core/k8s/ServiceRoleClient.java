package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.meta.K8sResourceEnum;
import me.snowdrop.istio.api.rbac.v1alpha1.ServiceRole;
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
public class ServiceRoleClient extends AbstractK8sClient<ServiceRole> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRoleClient.class);

    @Override
    public void createOrUpdate(List<ServiceRole> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            logger.warn("createOrUpdate failed. empty resources list");
            return;
        }
        resources.stream().forEach(r -> createOrUpdate(r));
    }

    @Override
    public void createOrUpdate(ServiceRole resource) {
        istioClient().serviceRole()
                .inNamespace(resource.getMetadata().getNamespace())
                .createOrReplace(resource);
    }

    @Override
    public void delete(String kind, String name, String namespace) {
        istioClient().serviceRole()
                .inNamespace(name).withName(name).delete();
    }

    @Override
    public List<ServiceRole> getList(String kind, String namespace) {
        return istioClient().serviceRole().inNamespace(namespace).list().getItems();
    }

    @Override
    public ServiceRole get(String kind, String name, String namespace) {
        return istioClient().serviceRole().inNamespace(namespace).withName(name).get();
    }

    @Override
    public boolean isAdapt(String kind) {
        return K8sResourceEnum.ServiceRole.equals(K8sResourceEnum.get(kind));
    }
}
