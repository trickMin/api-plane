package com.netease.cloud.nsf.service.impl;

import com.jayway.jsonpath.Criteria;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.WhiteList;
import com.netease.cloud.nsf.service.WhiteListService;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import com.sun.javafx.binding.StringFormatter;
import me.snowdrop.istio.api.rbac.v1alpha1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.netease.cloud.nsf.util.K8sResourceEnum.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/25
 **/
@Service
public class WhiteListServiceImpl implements WhiteListService {
    private static final Logger logger = LoggerFactory.getLogger(WhiteListServiceImpl.class);


    @Autowired
    private KubernetesClient client;

    @Autowired
    private TemplateTranslator templateTranslator;

    @Autowired
    private EditorContext editorContext;

    private static final String RBAC_TEMPLATE_NAME = "rbac";
    private static final String WHITELIST_TEMPLATE_NAME = "whiteList";
    private static final String YAML_SPLIT = "---";

    private ServiceRole getServiceRole(WhiteList whiteList) {
        return client.getObject(ServiceRole.name(), whiteList.getNamespace(), "qz-ingress");
    }

    private ServiceRoleBinding getServiceRoleBinding(WhiteList whiteList) {
        return client.getObject(ServiceRoleBinding.name(), whiteList.getNamespace(), "qz-ingress");
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
            client.createOrUpdate(yaml, ResourceType.YAML);
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
        generator.removeElement(PathExpressionEnum.REMOVE_RBAC_SERVICE.translate(),
                Criteria.where("services").contains(whiteList.getFullService()));
        generator.addElement(PathExpressionEnum.ADD_RBAC_SERVICE.translate(), rule);
        client.createOrUpdate(generator.jsonString(), ResourceType.JSON);

        //todo: 如果service已经存在virtualservice, destinationrule, 会把原来的覆盖掉
        String[] yamls = templateTranslator.translate(WHITELIST_TEMPLATE_NAME, whiteList, YAML_SPLIT);
        for (String yaml : yamls) {
            if (!yaml.contains("apiVersion")) {
                continue;
            }
            client.createOrUpdate(yaml, ResourceType.YAML);
        }
    }

    @Override
    public void removeService(WhiteList whiteList) {
        ServiceRole role = getServiceRole(whiteList);
        ResourceGenerator generator = ResourceGenerator.newInstance(role, ResourceType.OBJECT, editorContext);
        generator.removeElement(PathExpressionEnum.REMOVE_RBAC_SERVICE.translate(),
                Criteria.where("services").contains(whiteList.getService()));
        client.createOrUpdate(generator.jsonString(), ResourceType.JSON);

        String service = whiteList.getService();
        String namespace = whiteList.getNamespace();

        // todo: 可能会误删
        client.delete(VirtualService.name(), namespace, getVirtualServiceName(service));
        client.delete(DestinationRule.name(), namespace, getDestinationRuleName(service));
        client.delete(ServiceRole.name(), namespace, getServiceRoleName(service));
        client.delete(ServiceRoleBinding.name(), namespace, getServiceRoleBindingName(service));
        client.delete(Policy.name(), namespace, getPolicyName(service));
    }

    private AccessRule buildAccessRule(String service, List<String> values) {
        String key = "request.headers[Source-External]";

        return new AccessRuleBuilder().withServices(service)
                .withConstraints(new ConstraintBuilder().withKey(key).withValues(values).build()).build();
    }

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
