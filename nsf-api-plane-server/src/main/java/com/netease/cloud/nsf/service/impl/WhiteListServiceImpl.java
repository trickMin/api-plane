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
import me.snowdrop.istio.api.rbac.v1alpha1.AccessRule;
import me.snowdrop.istio.api.rbac.v1alpha1.AccessRuleBuilder;
import me.snowdrop.istio.api.rbac.v1alpha1.ConstraintBuilder;
import me.snowdrop.istio.api.rbac.v1alpha1.ServiceRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;


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

    private static final String RBAC_INGRESS_TEMPLATE_NAME = "rbac_ingress";
    private static final String WHITELIST_TEMPLATE_NAME = "whiteList";
    private static final String YAML_SPLIT = "---";

    private ServiceRole getIngressWhitelistServiceRole(WhiteList whiteList) {
        HasMetadata resource = integratedClient.get("qz-ingress-whitelist", whiteList.getNamespace(), K8sResourceEnum.ServiceRole.name());
        return resource == null ? null : (ServiceRole) resource;
    }

    private void initNamespaceIfNeeded(WhiteList whiteList) {
        if (integratedClient.get("qz-ingress-whitelist", whiteList.getNamespace(), K8sResourceEnum.ServiceRole.name()) == null ||
            integratedClient.get("qz-ingress-whitelist", whiteList.getNamespace(), K8sResourceEnum.ServiceRoleBinding.name()) == null ||
            integratedClient.get("qz-ingress-passed", whiteList.getNamespace(), K8sResourceEnum.ServiceRole.name()) == null ||
            integratedClient.get("qz-ingress-passed", whiteList.getNamespace(), K8sResourceEnum.ServiceRoleBinding.name()) == null
        ) {
            Arrays.stream(templateTranslator.translate(RBAC_INGRESS_TEMPLATE_NAME, whiteList, YAML_SPLIT))
                .filter(yaml -> yaml.contains("apiVersion"))
                .forEach(yaml -> integratedClient.createOrUpdate(yaml));
        }
    }

    /**
     * 要求每次都上报全量的values
     *
     * @param whiteList
     */
    @Override
    public void updateService(WhiteList whiteList) {
        initNamespaceIfNeeded(whiteList);
        addRuleToIngressWhitelist(whiteList);
        addRuleToIngressPassed(whiteList);

        //todo: 如果service已经存在virtualservice, destinationrule, 会把原来的覆盖掉
        String[] yamls = templateTranslator.translate(WHITELIST_TEMPLATE_NAME, whiteList, YAML_SPLIT);
        for (String yaml : yamls) {
            if (!yaml.contains("apiVersion")) {
                continue;
            }
            integratedClient.createOrUpdate(yaml);
        }
    }

    private void addRuleToIngressWhitelist(WhiteList whiteList) {
        ServiceRole ingressWhitelistRole = (ServiceRole) integratedClient.get("qz-ingress-whitelist", whiteList.getNamespace(), K8sResourceEnum.ServiceRole.name());
        ResourceGenerator whitelistGenerator = ResourceGenerator.newInstance(ingressWhitelistRole, ResourceType.OBJECT, editorContext);
        AccessRule iwRule = new AccessRuleBuilder()
            .withServices(whiteList.getFullService())
            .withConstraints(new ConstraintBuilder()
                .withKey("request.headers[Source-External]")
                .withValues(whiteList.getSources())
                .build())
            .build();
        whitelistGenerator.removeElement(PathExpressionEnum.YANXUAN_REMOVE_RBAC_SERVICE.translate(),
                Criteria.where("services").contains(whiteList.getFullService()));
        whitelistGenerator.addElement(PathExpressionEnum.YANXUAN_ADD_RBAC_SERVICE.translate(), iwRule);
        integratedClient.createOrUpdate(whitelistGenerator);
    }

    private void addRuleToIngressPassed(WhiteList whiteList) {
        ServiceRole ingressPassedRole = (ServiceRole) integratedClient.get("qz-ingress-passed", whiteList.getNamespace(), K8sResourceEnum.ServiceRole.name());
        ResourceGenerator passedGenerator = ResourceGenerator.newInstance(ingressPassedRole, ResourceType.OBJECT, editorContext);
        AccessRule passedRule = new AccessRuleBuilder()
            .withServices(whiteList.getFullService())
            .withPaths(whiteList.getConfigPassedPaths())
            .build();
        passedGenerator.removeElement(PathExpressionEnum.YANXUAN_REMOVE_RBAC_SERVICE.translate(),
            Criteria.where("services").contains(whiteList.getFullService()));
        passedGenerator.addElement(PathExpressionEnum.YANXUAN_ADD_RBAC_SERVICE.translate(), passedRule);
        integratedClient.createOrUpdate(passedGenerator);
    }

    @Override
    public void removeService(WhiteList whiteList) {
        ServiceRole role = getIngressWhitelistServiceRole(whiteList);
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
