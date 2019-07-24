package com.netease.cloud.nsf.meta;


import com.netease.cloud.nsf.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.HasMetadata;

/**
 * @auther wupenghuai@corp.netease.com
 * @date 2019/7/23
 **/
public enum ResourceEnum {
    VirtualService("virtualservices.networking.istio.io", IstioResource.class),
    DestinationRule("destinationrules.networking.istio.io", IstioResource.class),
    ServiceRole("serviceroles.rbac.istio.io", IstioResource.class),
    ServiceRoleBinding("servicerolebindings.rbac.istio.io", IstioResource.class),
    Policy("policies.authentication.istio.io", IstioResource.class),
    ServiceAccount("serviceaccounts", io.fabric8.kubernetes.api.model.ServiceAccount.class);

    private String resourceName;
    private Class<? extends HasMetadata> mappingType;

    ResourceEnum(Class<? extends HasMetadata> mappingType) {
        this("", mappingType);
    }

    ResourceEnum(String resourceName, Class<? extends HasMetadata> mappingType) {
        this.resourceName = resourceName;
        this.mappingType = mappingType;
    }

    public String resourceName() {
        return resourceName;
    }

    public Class<? extends HasMetadata> mappingType() {
        return mappingType;
    }

    public static ResourceEnum get(String name) {
        for (ResourceEnum resourceEnum : values()) {
            if (resourceEnum.name().equalsIgnoreCase(name)) {
                return resourceEnum;
            }
        }
        throw new ApiPlaneException("Unsupported resource types");
    }
}
