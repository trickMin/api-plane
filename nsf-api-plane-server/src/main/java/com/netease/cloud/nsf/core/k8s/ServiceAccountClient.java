package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.meta.ResourceEnum;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
@Order(2)
@Component
public class ServiceAccountClient extends AbstractK8sClient<ServiceAccount> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceAccountClient.class);

    @Override
    public void createOrUpdate(List<ServiceAccount> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            logger.warn("createOrUpdate failed. empty resources list");
            return;
        }
        resources.stream().forEach(r -> createOrUpdate(r));
    }

    @Override
    public void createOrUpdate(ServiceAccount resource) {
        client().serviceAccounts()
                .inNamespace(resource.getMetadata().getNamespace())
                .createOrReplace(resource);
    }

    @Override
    public void delete(String kind, String name, String namespace) {
        client().serviceAccounts()
                .inNamespace(name)
                .withName(name)
                .delete();
    }

    @Override
    public List<ServiceAccount> getList(String kind, String namespace) {
        return client().serviceAccounts().inNamespace(namespace).list().getItems();
    }

    @Override
    public ServiceAccount get(String kind, String name, String namespace) {
        return client().serviceAccounts().inNamespace(namespace).withName(name).get();
    }

    @Override
    public boolean isAdapt(String kind) {
        return ResourceEnum.ServiceAccount.equals(ResourceEnum.get(kind));
    }
}
