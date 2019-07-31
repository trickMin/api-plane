package com.netease.cloud.nsf.meta;


import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.IstioResource;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
public enum K8sResourceEnum {
    VirtualService("virtualservices.networking.istio.io", me.snowdrop.istio.api.networking.v1alpha3.VirtualService.class),
    DestinationRule("destinationrules.networking.istio.io", me.snowdrop.istio.api.networking.v1alpha3.DestinationRule.class),
    ServiceRole("serviceroles.rbac.istio.io", me.snowdrop.istio.api.rbac.v1alpha1.ServiceRole.class),
    ServiceRoleBinding("servicerolebindings.rbac.istio.io", me.snowdrop.istio.api.rbac.v1alpha1.ServiceRoleBinding.class),
    Policy("policies.authentication.istio.io", me.snowdrop.istio.api.authentication.v1alpha1.Policy.class),
    ServiceAccount("serviceaccounts", io.fabric8.kubernetes.api.model.ServiceAccount.class),
    Gateway("gateway.networking.istio.io", IstioResource.class),

    ;

    private String resourceName;
    private Class<? extends HasMetadata> mappingType;

    K8sResourceEnum(Class<? extends HasMetadata> mappingType) {
        this("", mappingType);
    }

    K8sResourceEnum(String resourceName, Class<? extends HasMetadata> mappingType) {
        this.resourceName = resourceName;
        this.mappingType = mappingType;
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
