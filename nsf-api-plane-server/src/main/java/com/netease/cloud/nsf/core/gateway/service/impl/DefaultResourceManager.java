package com.netease.cloud.nsf.core.gateway.service.impl;

import com.netease.cloud.nsf.core.istio.IstioHttpClient;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Gateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/17
 **/
@Component
public class DefaultResourceManager implements ResourceManager {

    @Autowired
    private IstioHttpClient istioHttpClient;

    @Override
    public List<Endpoint> getEndpointList() {
        return istioHttpClient.getEndpointList();
    }

    @Override
    public List<Gateway> getGatewayList() {
        return istioHttpClient.getGatewayList();
    }
}
