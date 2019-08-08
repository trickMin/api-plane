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
import me.snowdrop.istio.api.rbac.v1alpha1.AccessRule;
import me.snowdrop.istio.api.rbac.v1alpha1.AccessRuleBuilder;
import me.snowdrop.istio.api.rbac.v1alpha1.ConstraintBuilder;
import me.snowdrop.istio.api.rbac.v1alpha1.RbacConfig;
import me.snowdrop.istio.api.rbac.v1alpha1.ServiceRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

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

    private static final String RBAC_INGRESS_TEMPLATE_NAME = "rbac_ingress";
    private static final String WHITELIST_TEMPLATE_NAME = "whiteList";
    private static final String YAML_SPLIT = "---";

    private void initNamespaceIfNeeded(WhiteList whiteList) {
        if (client.get(ServiceRole.name(), whiteList.getNamespace(), "qz-ingress-whitelist") == null ||
            client.get(ServiceRoleBinding.name(), whiteList.getNamespace(), "qz-ingress-whitelist") == null ||
            client.get(ServiceRole.name(), whiteList.getNamespace(), "qz-ingress-passed") == null ||
            client.get(ServiceRoleBinding.name(), whiteList.getNamespace(), "qz-ingress-passed") == null) {
            Arrays.stream(templateTranslator.translate(RBAC_INGRESS_TEMPLATE_NAME, whiteList, YAML_SPLIT))
                .filter(yaml -> yaml.contains("apiVersion"))
                .forEach(yaml -> client.createOrUpdate(yaml, ResourceType.YAML));
		}
	}

    private void addItemToClusterRbacConfig(WhiteList whiteList) {
        RbacConfig rbacConfig = client.getObject(ClusterRbacConfig.name(), whiteList.getNamespace(), "default");
        ResourceGenerator generator = ResourceGenerator.newInstance(rbacConfig, ResourceType.OBJECT, editorContext);
		String fullService = whiteList.getFullService();
        if (rbacConfig.getSpec().getInclusion().getServices().contains(fullService)) {
            logger.info("rbac for {} already enabled.", fullService);
            return;
        }
        generator.addElement(PathExpressionEnum.ADD_RBAC_SERVICE.translate(), fullService);
        client.createOrUpdate(generator.jsonString(), ResourceType.JSON);
        logger.info("successfully enable rbac for {}.", fullService);
    }

    /**
     * 要求每次都上报全量的values
     *
     * @param whiteList
     */
    @Override
    public void updateService(WhiteList whiteList) {
    	initNamespaceIfNeeded(whiteList);
    	addItemToClusterRbacConfig(whiteList);
        addRuleToIngressWhitelist(whiteList);
        addRuleToIngressPassed(whiteList);
        createOrUpdateServiceWhitelist(whiteList);
    }

    private void createOrUpdateServiceWhitelist(WhiteList whiteList) {
        String[] yamls = templateTranslator.translate(WHITELIST_TEMPLATE_NAME, whiteList, YAML_SPLIT);
        for (String yaml : yamls) {
            if (!yaml.contains("apiVersion")) {
                continue;
            }
            client.createOrUpdate(yaml, ResourceType.YAML);
        }
    }

    private void addRuleToIngressWhitelist(WhiteList whiteList) {
        ServiceRole ingressWhitelistRole = client.getObject(ServiceRole.name(), whiteList.getNamespace(), "qz-ingress-whitelist");
        ResourceGenerator whitelistGenerator = ResourceGenerator.newInstance(ingressWhitelistRole, ResourceType.OBJECT, editorContext);

        AccessRule iwRule = new AccessRuleBuilder()
            .withServices(whiteList.getFullService())
            .withConstraints(new ConstraintBuilder()
                .withKey("request.headers[Source-External]")
                .withValues(whiteList.getSources())
                .build())
            .build();
        whitelistGenerator.removeElement(PathExpressionEnum.REMOVE_RBAC_SERVICE.translate(),
            Criteria.where("services").contains(whiteList.getFullService()));
        whitelistGenerator.addElement(PathExpressionEnum.ADD_RBAC_SERVICE.translate(), iwRule);
        client.createOrUpdate(whitelistGenerator.jsonString(), ResourceType.JSON);
    }

    private void addRuleToIngressPassed(WhiteList whiteList) {
        ServiceRole ingressPassedRole = client.getObject(ServiceRole.name(), whiteList.getNamespace(), "qz-ingress-passed");
        ResourceGenerator passedGenerator = ResourceGenerator.newInstance(ingressPassedRole, ResourceType.OBJECT, editorContext);

        AccessRule passedRule = new AccessRuleBuilder()
            .withServices(whiteList.getFullService())
            .withPaths(whiteList.getConfigPassedPaths())
            .build();
        passedGenerator.removeElement(PathExpressionEnum.REMOVE_RBAC_SERVICE.translate(),
            Criteria.where("services").contains(whiteList.getFullService()));
        passedGenerator.addElement(PathExpressionEnum.ADD_RBAC_SERVICE.translate(), passedRule);
        client.createOrUpdate(passedGenerator.jsonString(), ResourceType.JSON);
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
