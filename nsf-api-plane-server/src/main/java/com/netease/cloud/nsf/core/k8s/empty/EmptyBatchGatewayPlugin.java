package com.netease.cloud.nsf.core.k8s.empty;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import me.snowdrop.istio.api.networking.v1alpha3.GatewayPlugin;

import java.util.Map;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/3/19
 **/
public class EmptyBatchGatewayPlugin extends GatewayPlugin implements HasMetadata, EmptyBatchResource{

    private ObjectMeta om;

    public EmptyBatchGatewayPlugin(Map<String, String> labels) {
        ObjectMeta tom = new ObjectMeta();
        tom.setLabels(labels);
        this.om = tom;
    }

    @Override
    public ObjectMeta getMetadata() {
        return om;
    }

}
