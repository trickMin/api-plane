package com.netease.cloud.nsf.client;

import com.netease.cloud.nsf.exception.ApiPlaneException;
import com.netease.cloud.nsf.meta.ResourceEnum;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.EnumSet;
import java.util.List;

import static com.netease.cloud.nsf.meta.ResourceEnum.*;

/**
 * todo:
 *
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
@Order(100)
@Component
class K8sGeneralResourceClient extends AbstractK8sClient<HasMetadata> {

    private static final Logger logger = LoggerFactory.getLogger(K8sGeneralResourceClient.class);

    private static EnumSet<ResourceEnum> enums = EnumSet.of(ServiceAccount);


    @Override
    public void createOrUpdate(List<HasMetadata> resources) {
    }

    @Override
    public void createOrUpdate(HasMetadata resource) {
    }

    @Override
    public void delete(String kind, String name, String namespace) {
    }

    @Override
    public List<HasMetadata> getList(String kind, String namespace) {
        // todo:
        return null;
    }

    @Override
    public HasMetadata get(String kind, String name, String namespace) {
        return null;
    }

    @Override
    public boolean isAdapt(String kind) {
        return enums.contains(ResourceEnum.get(kind));
    }


    private HasMetadata blankResource(String kind, String name, String namespace) {
        try {
            Class<? extends HasMetadata> clz = ResourceEnum.get(kind).mappingType();
            HasMetadata resource = clz.newInstance();
            ObjectMeta meta = new ObjectMeta();
            meta.setNamespace(namespace);
            meta.setName(name);
            resource.setMetadata(meta);
            return resource;
        } catch (InstantiationException e) {
            e.printStackTrace();
            logger.error("Unsupported resource types : {}", kind);
            throw new ApiPlaneException("Unsupported resource types", e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            logger.error("Unsupported resource types : {}", kind);
            throw new ApiPlaneException("Unsupported resource types", e);
        }
    }
}
