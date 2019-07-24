package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.meta.DoneableIstioResource;
import com.netease.cloud.nsf.meta.IstioResource;
import com.netease.cloud.nsf.meta.IstioResourceList;
import com.netease.cloud.nsf.meta.ResourceEnum;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.EnumSet;
import java.util.List;

import static com.netease.cloud.nsf.meta.ResourceEnum.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
@Order(1)
@Component
class K8sCustomResourceClient extends AbstractK8sClient<IstioResource> {

    private static final Logger logger = LoggerFactory.getLogger(K8sCustomResourceClient.class);

    private static EnumSet<ResourceEnum> enums = EnumSet.of(
            VirtualService,
            DestinationRule,
            ServiceRole,
            ServiceRoleBinding,
            Policy
    );

    @Override
    public void createOrUpdate(List<IstioResource> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            logger.warn("createOrUpdate failed. empty resources list");
            return;
        }
        resources.stream().forEach(r -> createOrUpdate(r));
    }

    @Override
    public void createOrUpdate(IstioResource resource) {
        String kind = resource.getKind();
        ResourceEnum crd = ResourceEnum.get(kind);
        getOperation(crd.resourceName())
                .inNamespace(resource.getMetadata().getNamespace())
                .createOrReplace(resource);
    }

    @Override
    public void delete(String kind, String name, String namespace) {
        ResourceEnum crd = ResourceEnum.get(kind);
        IstioResource resource = new IstioResource(crd.resourceName(), name, namespace, null);
        getOperation(crd.resourceName()).inNamespace(namespace).delete(resource);
    }

    @Override
    public List<IstioResource> getList(String kind, String namespace) {
        ResourceEnum crd = ResourceEnum.get(kind);
        return getOperation(crd.resourceName())
                .inNamespace(namespace)
                .list()
                .getItems();
    }

    @Override
    public IstioResource get(String kind, String name, String namespace) {
        ResourceEnum crd = ResourceEnum.get(kind);
        return getOperation(crd.resourceName())
                .inNamespace(namespace)
                .withName(name)
                .get();
    }

    @Override
    public boolean isAdapt(String kind) {
        return enums.contains(ResourceEnum.get(kind));
    }

    private MixedOperation<IstioResource, IstioResourceList,
            DoneableIstioResource, Resource<IstioResource, DoneableIstioResource>> getOperation(String crdType) {
        CustomResourceDefinition crd = client().customResourceDefinitions().withName(crdType).get();
        return client().customResources(crd, IstioResource.class, IstioResourceList.class, DoneableIstioResource.class);

    }
}
