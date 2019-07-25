package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.meta.K8sResourceEnum;
import me.snowdrop.istio.api.networking.v1alpha3.VirtualService;
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
public class VirtualServiceClient extends AbstractK8sClient<VirtualService> {
    private static final Logger logger = LoggerFactory.getLogger(VirtualServiceClient.class);

    @Override
    public void createOrUpdate(List<VirtualService> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            logger.warn("createOrUpdate failed. empty resources list");
            return;
        }
        resources.stream().forEach(r -> createOrUpdate(r));
    }

    @Override
    public void createOrUpdate(VirtualService resource) {
        istioClient().virtualService()
                .inNamespace(resource.getMetadata().getNamespace())
                .createOrReplace(resource);
    }

    @Override
    public void delete(String kind, String name, String namespace) {
        istioClient().virtualService()
                .inNamespace(name).withName(name).delete();
    }

    @Override
    public List<VirtualService> getList(String kind, String namespace) {
        return istioClient().virtualService().inNamespace(namespace).list().getItems();
    }

    @Override
    public VirtualService get(String kind, String name, String namespace) {
        return istioClient().virtualService().inNamespace(namespace).withName(name).get();
    }

    @Override
    public boolean isAdapt(String kind) {
        return K8sResourceEnum.VirtualService.equals(K8sResourceEnum.get(kind));
    }
}
