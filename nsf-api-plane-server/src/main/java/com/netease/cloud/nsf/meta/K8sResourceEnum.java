package com.netease.cloud.nsf.meta;


import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.sun.javafx.binding.StringFormatter;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.utils.URLUtils;
import me.snowdrop.istio.api.authentication.v1alpha1.*;
import me.snowdrop.istio.api.networking.v1alpha3.*;
import me.snowdrop.istio.api.rbac.v1alpha1.*;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
public enum K8sResourceEnum {
    VirtualService("virtualservices.networking.istio.io", VirtualService.class, VirtualServiceList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/virtualservices"),
    DestinationRule("destinationrules.networking.istio.io", DestinationRule.class, DestinationRuleList.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/destinationrules"),
    ServiceRole("serviceroles.rbac.istio.io", ServiceRole.class, ServiceRoleList.class, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/serviceroles"),
    ServiceRoleBinding("servicerolebindings.rbac.istio.io", ServiceRoleBinding.class, ServiceRoleBindingList.class, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/servicerolebindings"),
    Policy("policies.authentication.istio.io", Policy.class, PolicyList.class, "/apis/authentication.istio.io/v1alpha1/namespaces/%s/policies"),
    ServiceAccount("serviceaccounts", ServiceAccount.class, ServiceAccountList.class, "/api/v1/namespaces/%s/serviceaccounts"),
    Pod("pods", Pod.class, PodList.class, "/api/v1/namespaces/%s/pods"),
    ;

    private String resourceName;
    private Class<? extends HasMetadata> mappingType;
    private Class<? extends KubernetesResourceList> mappingListType;
    private String selfLink;


    K8sResourceEnum(String resourceName, Class<? extends HasMetadata> mappingType, Class<? extends KubernetesResourceList> mappingListType, String selfLink) {
        this.resourceName = resourceName;
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

    public String resourceName() {
        return resourceName;
    }

    public Class<? extends HasMetadata> mappingType() {
        return mappingType;
    }

    public Class<? extends KubernetesResourceList> mappingListType() {
        return mappingListType;
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
