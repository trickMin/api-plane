package com.netease.cloud.nsf.meta;


import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.sun.javafx.binding.StringFormatter;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.utils.URLUtils;
import me.snowdrop.istio.api.authentication.v1alpha1.*;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import me.snowdrop.istio.api.rbac.v1alpha1.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
public enum K8sResourceEnum {
    VirtualService(VirtualService.class, VirtualServiceList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/virtualservices"),
    DestinationRule(DestinationRule.class, DestinationRuleList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/destinationrules"),
    ServiceRole(ServiceRole.class, ServiceRoleList.class, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/serviceroles"),
    ServiceRoleBinding(ServiceRoleBinding.class, ServiceRoleBindingList.class, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/servicerolebindings"),
    Policy(Policy.class, PolicyList.class, "/apis/authentication.istio.io/v1alpha1/namespaces/%s/policies"),
    ServiceAccount(ServiceAccount.class, ServiceAccountList.class, "/api/v1/namespaces/%s/serviceaccounts"),
    Gateway(me.snowdrop.istio.api.networking.v1alpha3.Gateway.class, GatewayList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/gateways"),
    Pod(Pod.class, PodList.class, "/api/v1/namespaces/%s/pods"),
    ;

    private Class<? extends HasMetadata> mappingType;
    private Class<? extends KubernetesResourceList> mappingListType;
    private String selfLink;


    K8sResourceEnum(Class<? extends HasMetadata> mappingType, Class<? extends KubernetesResourceList> mappingListType, String selfLink) {
        this.mappingType = mappingType;
        this.mappingListType = mappingListType;
        this.selfLink = selfLink;
    }


    public String selfLink() {
        return selfLink;
    }

    public String selfLink(String namespace) {
        return StringFormatter.format(selfLink, namespace).getValue();
    }

    public String selfLink(String masterUrl, String namespace) {
        return URLUtils.pathJoin(masterUrl, selfLink(namespace));
    }

    public Class<? extends HasMetadata> mappingType() {
        return mappingType;
    }

    public Class<? extends KubernetesResourceList> mappingListType() {
        return mappingListType;
    }

    public static K8sResourceEnum getElement(String name) {
        Pattern pattern = Pattern.compile("(.*)List$");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            return get(matcher.group(1));
        }
        return get(name);
    }

    public static K8sResourceEnum get(String name) {
        for (K8sResourceEnum k8sResourceEnum : values()) {
            if (k8sResourceEnum.name().equalsIgnoreCase(name)) {
                return k8sResourceEnum;
            }
        }
        throw new ApiPlaneException("Unsupported resource types");
    }
}
