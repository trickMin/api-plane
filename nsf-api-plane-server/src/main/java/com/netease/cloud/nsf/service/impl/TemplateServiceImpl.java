package com.netease.cloud.nsf.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.netease.cloud.nsf.core.k8s.IntegratedClient;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.meta.ResourceEnum;
import com.netease.cloud.nsf.meta.template.Metadata;
import com.netease.cloud.nsf.meta.template.NsfExtra;
import com.netease.cloud.nsf.meta.template.ServiceMeshTemplate;
import com.netease.cloud.nsf.service.TemplateService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
@Service
public class TemplateServiceImpl implements TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateServiceImpl.class);


    @Autowired
    private Configuration configuration;

    @Autowired
    private IntegratedClient client;

    @Autowired
    @Qualifier("yaml")
    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    private static final String YAML_SPLIT = "---";

    public void updateConfig(ServiceMeshTemplate template) {
        template.setUpdate(true);
        String content = translate(template);
        logger.info("update config : \r" + content);
        client.createOrUpdate(string2Resources(content));
    }


    /**
     * 找到模板，并填充模板中的占位符
     *
     * @param rawTemplate
     * @return
     */
    private String translate(ServiceMeshTemplate rawTemplate) {
        String content = null;
        try {
            Template template = configuration.getTemplate(rawTemplate.getNsfTemplate() + ".ftl");
            content = FreeMarkerTemplateUtils.processTemplateIntoString(template, rawTemplate);
        } catch (IOException e) {
            logger.warn("get template failed", e);
            throw new ApiPlaneException(e.getMessage(), e);
        } catch (TemplateException e) {
            logger.warn("parse template failed", e);
            throw new ApiPlaneException(e.getMessage(), e);
        }
        return content;
    }

    /**
     * 将模板的内容转换为k8s资源
     *
     * @param content
     * @return
     */
    private List<HasMetadata> string2Resources(String content) {

        if (StringUtils.isEmpty(content)) throw new IllegalArgumentException("yaml content");
        List<HasMetadata> resources = new ArrayList<>();
        try {
            for (String segment : content.split(YAML_SPLIT)) {
                if (!segment.contains("apiVersion")) continue;
                String kind = getKind(segment);
                Class<? extends HasMetadata> clz = ResourceEnum.get(kind).mappingType();
                resources.add(mapper.readValue(segment, clz));
            }
        } catch (Exception e) {
            logger.warn("convert string to istio resource failed", e);
            throw new ApiPlaneException(e.getMessage(), e);
        }
        return resources;
    }

    /**
     * 根据模板内容，找到kind类型
     *
     * @param content
     * @return
     */
    private String getKind(String content) {
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(content);
        } catch (IOException e) {
            logger.warn("read tree failed", e);
            throw new ApiPlaneException(e.getMessage(), e);
        }
        String kind = jsonNode.get("kind").asText();
        return kind;
    }


    public void deleteConfig(String name, String namespace, String kind) {
        logger.info("delete config by name: {}, namespace: {}, kind: {}", name, namespace, kind);
        client.deleteByName(name, namespace, kind);
    }

    public void deleteConfigByTemplate(String name, String namespace, String templateName) {
        logger.info("delete config by name: {}, namespace: {}, templateName: {}", name, namespace, templateName);

        ServiceMeshTemplate template = blankTemplate(name, namespace, templateName);
        List<HasMetadata> resources = string2Resources(translate(template));
        if (!CollectionUtils.isEmpty(resources)) {
            resources.stream()
                    .forEach(r -> deleteConfig(r.getMetadata().getName(), r.getMetadata().getNamespace(), r.getKind()));
        }
    }

    public HasMetadata getConfig(String name, String namespace, String kind) {
        logger.info("get config by name: {}, namespace: {}, kind: {}", name, namespace, kind);
        return client.get(name, namespace, kind);
    }

    public List<HasMetadata> getConfigList(String namespace, String kind) {
        logger.info("get config by namespace: {}, kind: {}", namespace, kind);
        return client.getResources(namespace, kind);
    }

    public List<HasMetadata> getConfigListByTemplate(String name, String namespace, String templateName) {
        List<HasMetadata> remoteResources = new ArrayList<>();
        ServiceMeshTemplate template = blankTemplate(name, namespace, templateName);
        List<HasMetadata> resources = string2Resources(translate(template));
        if (!CollectionUtils.isEmpty(resources)) {
            resources.stream()
                    .forEach(r -> {
                        HasMetadata config = getConfig(r.getMetadata().getName(), r.getMetadata().getNamespace(), r.getKind());
                        if (config != null) {
                            remoteResources.add(config);
                        }
                    });
        }
        return remoteResources;
    }

    /**
     * 生成一个空模板，用于删除或查询操作，
     * 主要为填充模板的占位符，才能顺利解析，然后得到模板的kind
     *
     * @param name
     * @param namespace
     * @param templateName
     * @return
     */
    private ServiceMeshTemplate blankTemplate(String name, String namespace, String templateName) {
        return ServiceMeshTemplate.ServiceMeshTemplateBuilder.aServiceMeshTemplate()
                .withMetadata(Metadata.MetadataBuilder.aMetadata()
                        .withName(name)
                        .withNamespace(namespace)
                        .build())
                .withNsfExtra(NsfExtra.NsfExtraBuilder.aNsfExtra()
                        .build())
                .withNsfTemplate(templateName)
                .build();
    }

}
