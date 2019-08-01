package com.netease.cloud.nsf.core.k8s;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
@Deprecated
@Component
public class IntegratedClient {
    private static final Logger logger = LoggerFactory.getLogger(IntegratedClient.class);

    @Autowired
    private EditorContext context;

    @Autowired
    private KubernetesClient client;

    public void createOrUpdate(ResourceGenerator generator) {
        String kind = generator.getValue(PathExpressionEnum.YANXUAN_GET_KIND.translate());
        HasMetadata object = generator.object(K8sResourceEnum.get(kind).mappingType());
        createOrUpdate(object);
    }

    public void createOrUpdate(String yaml) {
        ResourceGenerator generator = ResourceGenerator.newInstance(yaml, ResourceType.YAML, context);
        createOrUpdate(generator);
    }

    public void createOrUpdate(List<HasMetadata> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            logger.warn("createOrUpdate failed. empty resources list");
            return;
        }
        resources.stream().forEach(r -> createOrUpdate(r));
    }

    public void createOrUpdate(HasMetadata resource) {
        client.createOrUpdate(resource, ResourceType.OBJECT);
    }

    public void deleteByName(String name, String namespace, String type) {
        client.delete(type, namespace, name);
    }

    public HasMetadata get(String name, String namespace, String type) {
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(type);
        return client.getObject(type, namespace, name);
    }

}
