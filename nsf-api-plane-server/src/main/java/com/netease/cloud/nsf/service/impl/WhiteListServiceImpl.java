package com.netease.cloud.nsf.service.impl;

import com.jayway.jsonpath.Criteria;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.IntegratedClient;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.K8sResourceEnum;
import com.netease.cloud.nsf.meta.WhiteList;
import com.netease.cloud.nsf.service.WhiteListService;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.rbac.v1alpha1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
@Service
public class WhiteListServiceImpl implements WhiteListService {
    private static final Logger logger = LoggerFactory.getLogger(WhiteListServiceImpl.class);


    @Autowired
    private IntegratedClient integratedClient;

    @Autowired
    private TemplateTranslator templateTranslator;

    @Autowired
    private EditorContext editorContext;

    private static final String TEMPLATE_NAME = "rbac";
    private static final String YAML_SPLIT = "---";

    private ServiceRole getServiceRole(WhiteList whiteList) {
        HasMetadata resource = integratedClient.get(whiteList.getName(), whiteList.getNamespace(), K8sResourceEnum.ServiceRole.name());
        return resource == null ? null : (ServiceRole) resource;
    }

    /**
     * 初始化ServiceRole和ServiceRoleBinding资源
     */
    @Override
    public void initResource(WhiteList whiteList) {
        String[] yamls = templateTranslator.translate(TEMPLATE_NAME, whiteList, YAML_SPLIT);
        for (String yaml : yamls) {
            if (!yaml.contains("apiVersion")) {
                continue;
            }
            integratedClient.createOrUpdate(yaml);
        }
    }

    /**
     * 要求每次都上报全量的values
     *
     * @param whiteList
     */
    @Override
    public void updateService(WhiteList whiteList) {
        ServiceRole role = getServiceRole(whiteList);
        ResourceGenerator generator = ResourceGenerator.newInstance(role, ResourceType.OBJECT, editorContext);
        AccessRule rule = new AccessRuleBuilder().withServices(whiteList.getService())
                .withConstraints(new ConstraintBuilder().withKey(whiteList.getHeader()).withValues(whiteList.getValues()).build()).build();
        generator.removeElement(PathExpressionEnum.YANXUAN_REMOVE_RBAC_SERVICE.translate(),
                Criteria.where("services").contains(whiteList.getService()));
        generator.addElement(PathExpressionEnum.YANXUAN_ADD_RBAC_SERVICE.translate(), rule);
        integratedClient.createOrUpdate(generator);
    }

    @Override
    public void removeService(WhiteList whiteList) {
        ServiceRole role = getServiceRole(whiteList);
        ResourceGenerator generator = ResourceGenerator.newInstance(role, ResourceType.OBJECT, editorContext);
        generator.removeElement(PathExpressionEnum.YANXUAN_REMOVE_RBAC_SERVICE.translate(),
                Criteria.where("services").contains(whiteList.getService()));
        integratedClient.createOrUpdate(generator);
    }
}
