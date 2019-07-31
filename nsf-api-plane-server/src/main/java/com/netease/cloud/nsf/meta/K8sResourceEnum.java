package com.netease.cloud.nsf.meta;


import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.sun.javafx.binding.StringFormatter;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.utils.URLUtils;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
public enum K8sResourceEnum {
    VirtualService("virtualservices.networking.istio.io", me.snowdrop.istio.api.networking.v1alpha3.VirtualService.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/virtualservices"),
    DestinationRule("destinationrules.networking.istio.io", me.snowdrop.istio.api.networking.v1alpha3.DestinationRule.class, "/apis/networking.istio.io/v1alpha3/namespaces/%s/destinationrules"),
    ServiceRole("serviceroles.rbac.istio.io", me.snowdrop.istio.api.rbac.v1alpha1.ServiceRole.class, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/serviceroles"),
    ServiceRoleBinding("servicerolebindings.rbac.istio.io", me.snowdrop.istio.api.rbac.v1alpha1.ServiceRoleBinding.class, "/apis/rbac.istio.io/v1alpha1/namespaces/%s/servicerolebindings"),
    Policy("policies.authentication.istio.io", me.snowdrop.istio.api.authentication.v1alpha1.Policy.class, "/apis/authentication.istio.io/v1alpha1/namespaces/%s/policies"),
    ServiceAccount("serviceaccounts", io.fabric8.kubernetes.api.model.ServiceAccount.class, "/api/v1/namespaces/%s/serviceaccounts");

    private String resourceName;
    private Class<? extends HasMetadata> mappingType;
    private String selfLink;


    K8sResourceEnum(String resourceName, Class<? extends HasMetadata> mappingType, String selfLink) {
        this.resourceName = resourceName;
        this.mappingType = mappingType;
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

    public static K8sResourceEnum get(String name) {
        for (K8sResourceEnum k8sResourceEnum : values()) {
            if (k8sResourceEnum.name().equalsIgnoreCase(name)) {
                return k8sResourceEnum;
            }
        }
        throw new ApiPlaneException("Unsupported resource types");
    }
}
