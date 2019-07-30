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
import com.sun.javafx.binding.StringFormatter;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.rbac.v1alpha1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


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

    private static final String RBAC_TEMPLATE_NAME = "rbac";
    private static final String WHITELIST_TEMPLATE_NAME = "whiteList";
    private static final String YAML_SPLIT = "---";

    private ServiceRole getServiceRole(WhiteList whiteList) {
        HasMetadata resource = integratedClient.get("ingress", whiteList.getNamespace(), K8sResourceEnum.ServiceRole.name());
        return resource == null ? null : (ServiceRole) resource;
    }

    private ServiceRoleBinding getServiceRoleBinding(WhiteList whiteList) {
        HasMetadata resource = integratedClient.get("ingress", whiteList.getNamespace(), K8sResourceEnum.ServiceRoleBinding.name());
        return resource == null ? null : (ServiceRoleBinding) resource;
    }

    /**
     * 初始化ServiceRole和ServiceRoleBinding资源
     */
    public void initResource(WhiteList whiteList) {
        String[] yamls = templateTranslator.translate(RBAC_TEMPLATE_NAME, whiteList, YAML_SPLIT);
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
        if (getServiceRole(whiteList) == null || getServiceRoleBinding(whiteList) == null) {
            initResource(whiteList);
        }

        ServiceRole role = getServiceRole(whiteList);
        ResourceGenerator generator = ResourceGenerator.newInstance(role, ResourceType.OBJECT, editorContext);
        AccessRule rule = buildAccessRule(whiteList.getFullService(), whiteList.getSources());
        generator.removeElement(PathExpressionEnum.YANXUAN_REMOVE_RBAC_SERVICE.translate(),
                Criteria.where("services").contains(whiteList.getFullService()));
        generator.addElement(PathExpressionEnum.YANXUAN_ADD_RBAC_SERVICE.translate(), rule);
        integratedClient.createOrUpdate(generator);

        //todo: 如果service已经存在virtualservice, destinationrule, 会把原来的覆盖掉
        String[] yamls = templateTranslator.translate(WHITELIST_TEMPLATE_NAME, whiteList, YAML_SPLIT);
        for (String yaml : yamls) {
            if (!yaml.contains("apiVersion")) {
                continue;
            }
            integratedClient.createOrUpdate(yaml);
        }
    }

    @Override
    public void removeService(WhiteList whiteList) {
        ServiceRole role = getServiceRole(whiteList);
        ResourceGenerator generator = ResourceGenerator.newInstance(role, ResourceType.OBJECT, editorContext);
        generator.removeElement(PathExpressionEnum.YANXUAN_REMOVE_RBAC_SERVICE.translate(),
                Criteria.where("services").contains(whiteList.getService()));
        integratedClient.createOrUpdate(generator);

        String service = whiteList.getService();
        String namespace = whiteList.getNamespace();

        // todo: 可能会误删
        integratedClient.deleteByName(getVirtualServiceName(service), namespace, K8sResourceEnum.VirtualService.name());
        integratedClient.deleteByName(getDestinationRuleName(service), namespace, K8sResourceEnum.DestinationRule.name());
        integratedClient.deleteByName(getServiceRoleName(service), namespace, K8sResourceEnum.ServiceRole.name());
        integratedClient.deleteByName(getServiceRoleBindingName(service), namespace, K8sResourceEnum.ServiceRoleBinding.name());
        integratedClient.deleteByName(getPolicyName(service), namespace, K8sResourceEnum.Policy.name());
    }

    private AccessRule buildAccessRule(String service, List<String> values) {
        String key = "request.headers[Source-External]";

        return new AccessRuleBuilder().withServices(service)
                .withConstraints(new ConstraintBuilder().withKey(key).withValues(values).build()).build();
    }

    //todo: 需要给istio-resouce的命名制定规范
    private String getVirtualServiceName(String service) {
        return StringFormatter.format("%s", service).getValue();
    }

    private String getDestinationRuleName(String service) {
        return StringFormatter.format("%s", service).getValue();
    }

    private String getServiceRoleName(String service) {
        return StringFormatter.format("%s", service).getValue();
    }

    private String getServiceRoleBindingName(String service) {
        return StringFormatter.format("%s", service).getValue();
    }

    private String getPolicyName(String service) {
        return StringFormatter.format("%s", service).getValue();
    }
}
