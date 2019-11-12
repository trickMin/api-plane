package com.netease.cloud.nsf.core.gateway.service.impl;

import com.netease.cloud.nsf.core.istio.IstioHttpClient;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Gateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/17
 **/
@Component
public class DefaultResourceManager implements ResourceManager {

    @Autowired
    private IstioHttpClient istioHttpClient;

    @Override
    public List<Endpoint> getEndpointList() {
        return istioHttpClient.getEndpointList(endpoint ->
                endpoint.getAddress() != null &&
                        endpoint.getHostname() != null &&
                        endpoint.getPort() != null &&
                        inIstioSystem(endpoint.getHostname()) &&
                        inKubeSystem(endpoint.getHostname())
        );
    }

    @Override
    public List<Gateway> getGatewayList() {
        return istioHttpClient.getGatewayList(gateway -> gateway.getLabels().containsKey("gw_cluster"));
    }

    @Override
    public List<String> getServiceList() {
        return istioHttpClient.getServiceList(endpoint ->
                endpoint.getHostname() != null &&
                        inIstioSystem(endpoint.getHostname()) &&
                        inKubeSystem(endpoint.getHostname())
        );
    }

    private boolean inIstioSystem(String hostName) {
        return Pattern.compile("(.*)\\.istio-system\\.(.*)\\.(.*)\\.(.*)").matcher(hostName).find();
    }

    private boolean inKubeSystem(String hostName) {
        return Pattern.compile("(.*)\\.kube-system\\.(.*)\\.(.*)\\.(.*)").matcher(hostName).find();
    }
}
