package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.gateway.processor.ModelProcessor;
import com.netease.cloud.nsf.core.plugin.FragmentWrapper;
import com.netease.cloud.nsf.meta.Endpoint;

import java.util.List;

/**
 * yx用，一个service对应一个vs
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/22
 **/
public class YxVirtualServiceAPIDataHandler extends BaseVirtualServiceAPIDataHandler {

    public YxVirtualServiceAPIDataHandler(ModelProcessor subModelProcessor, List<FragmentWrapper> fragments, List<Endpoint> endpoints, boolean simple) {
        super(subModelProcessor, fragments, endpoints, simple);
    }

    @Override
    String buildVirtualServiceName(String serviceName, String apiName, String gw) {
        return String.format("%s-%s", serviceName, gw);
    }
}
