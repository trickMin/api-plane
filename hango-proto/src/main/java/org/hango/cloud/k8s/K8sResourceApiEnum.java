package org.hango.cloud.k8s;

/**
* @Author: zhufengwei.sx
* @Date: 2022/8/26 14:39
**/
public enum K8sResourceApiEnum {
    VIRTUAL_SERVICE("virtualservices.networking.istio.io"),
    DESTINATION_RULE("destinationrules.networking.istio.io"),
    ENVOY_PLUGIN("envoyplugins.microservice.slime.io"),
    KUBERNETES_GATEWAY("gateways.gateway.networking.k8s.io"),
    SMART_LIMITER("smartlimiters.microservice.slime.io"),
    HTTP_ROUTE("httproutes.gateway.networking.k8s.io");

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
        for (K8sResourceApiEnum value : values()) {
            if (value.name().equals(name)){
                return value;
            }
        }
        return null;
    }

}
