package com.netease.cloud.nsf.service.impl;

import com.jayway.jsonpath.Criteria;
import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.PathExpressionEnum;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.WhiteList;
import com.netease.cloud.nsf.service.WhiteListService;
import com.sun.javafx.binding.StringFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.netease.cloud.nsf.core.k8s.K8sResourceEnum.*;

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

    private static final String RBAC_INGRESS_TEMPLATE_NAME = "rbac_ingress";
    private static final String WHITELIST_TEMPLATE_NAME = "whiteList";
    private static final String YAML_SPLIT = "---";

    /**
     * 要求每次都上报全量的values
     *
     * @param whiteList
     */
    @Override
    public void updateService(WhiteList whiteList) {
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
        String role = client.get(ServiceRole.name(), whiteList.getNamespace(), "qz-ingress-whitelist");
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
