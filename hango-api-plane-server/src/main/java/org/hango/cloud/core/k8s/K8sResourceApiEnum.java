package org.hango.cloud.core.k8s;

import java.util.stream.Stream;

/**
* @Author: zhufengwei.sx
* @Date: 2022/8/26 14:39
**/
public enum K8sResourceApiEnum {
    VirtualService("virtualservices.networking.istio.io"),
    DestinationRule("destinationrules.networking.istio.io"),
    EnvoyPlugin("envoyplugins.microservice.slime.io");

    String api;
    K8sResourceApiEnum(String api){
        this.api = api;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public static K8sResourceApiEnum getByName(String name){
        return Stream.of(values()).filter(o -> o.name().equals(name)).findFirst().orElse(null);
    }

}
