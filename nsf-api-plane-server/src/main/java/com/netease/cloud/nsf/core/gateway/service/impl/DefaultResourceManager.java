package com.netease.cloud.nsf.core.gateway.service.impl;

import com.netease.cloud.nsf.core.envoy.EnvoyHttpClient;
import com.netease.cloud.nsf.core.istio.IstioHttpClient;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.ServiceHealth;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/17
 **/
@Component
public class DefaultResourceManager implements ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResourceManager.class);

    @Autowired
    private IstioHttpClient istioHttpClient;

    @Autowired
    private EnvoyHttpClient envoyHttpClient;

    @Override
    public List<Endpoint> getEndpointList() {
        logger.info("DefaultResourceManager.getEndpointList, Condition:{}", "address != null && hostname != null && port != null && !inIstioSystem() && !inKubeSystem() && !inGatewaySystem()");
        return istioHttpClient.getEndpointList(endpoint ->
                // address, hostname, port不为空
                endpoint.getAddress() != null &&
                        endpoint.getHostname() != null &&
                        endpoint.getPort() != null &&
                        // 不在 istio-system, kube-system, gateway-system内
                        !inIstioSystem(endpoint.getHostname()) &&
                        !inKubeSystem(endpoint.getHostname()) &&
                        !inGatewaySystem(endpoint.getHostname())
//                        // 是http服务
//                        &&isHttp(endpoint.getProtocol())
        );
    }

    @Override
    public List<Gateway> getGatewayList() {
        logger.info("DefaultResourceManager.getGatewayList, Condition:{}", "Objects.nonNull(Labels()) && gateway.getLabels().containsKey(\"gw_cluster\")");
        return istioHttpClient.getGatewayList(gateway ->
                // 包含gw_cluster label
                Objects.nonNull(gateway.getLabels()) &&
                        gateway.getLabels().containsKey("gw_cluster"));
    }

    @Override
    public List<String> getServiceList() {
        logger.info("DefaultResourceManager.getServiceList, Condition:{}", "hostname != null && !inIstioSystem() && !inKubeSystem() && !inGatewaySystem() && !isServiceEntry()");
        return istioHttpClient.getServiceList(endpoint ->
                // hostname不为空
                endpoint.getHostname() != null &&
                        // 不在 istio-system, kube-system, gateway-system内
                        !inIstioSystem(endpoint.getHostname()) &&
                        !inKubeSystem(endpoint.getHostname()) &&
                        !inGatewaySystem(endpoint.getHostname()) &&
//                        // 是http服务
//                        isHttp(endpoint.getProtocol()) &&
                        // 不是ServiceEntry服务
                        !isServiceEntry(endpoint.getHostname())
        );
    }

    @Override
    public Integer getServicePort(List<Endpoint> endpoints, String targetHost) {
        if (CollectionUtils.isEmpty(endpoints) || StringUtils.isBlank(targetHost)) {
            throw new ApiPlaneException("Get port by targetHost fail. param cant be null.");
        }
        List<Integer> ports = new ArrayList<>();
        for (Endpoint endpoint : endpoints) {
            if (targetHost.equals(endpoint.getHostname())) {
                ports.add(endpoint.getPort());
            }
        }
        if (ports.size() == 0) {
            throw new ApiPlaneException(String.format("Target endpoint %s does not exist", targetHost));
        } else if (ports.size() > 1) {
            logger.warn("There are multi port in service:[{}]. port:{}. use port:{}.", targetHost, ports, ports.get(0));
        }
        return ports.get(0);
    }

    @Override
    public List<ServiceHealth> getServiceHealthList(String host) {
        List<ServiceHealth> serviceHealth = envoyHttpClient.getServiceHealth(name -> {
            if (!name.contains("|")) return name;
            return name.substring(name.lastIndexOf("|") + 1);
        }, s -> s.equals(host));

        return serviceHealth;
    }


    private boolean inIstioSystem(String hostName) {
        return Pattern.compile("(.*)\\.istio-system\\.(.*)\\.(.*)\\.(.*)").matcher(hostName).find();
    }

    private boolean inKubeSystem(String hostName) {
        return Pattern.compile("(.*)\\.kube-system\\.(.*)\\.(.*)\\.(.*)").matcher(hostName).find();
    }

    private boolean inGatewaySystem(String hostName) {
        return Pattern.compile("(.*)\\.gateway-system\\.(.*)\\.(.*)\\.(.*)").matcher(hostName).find();
    }

    private boolean isHttp(String protocol) {
        return Pattern.compile(".*http(?!s).*").matcher(protocol).find();
    }

    private boolean isHttps(String protocol) {
        return Pattern.compile(".*https.*").matcher(protocol).find();
    }

    private boolean isServiceEntry(String hostname) {
        return Pattern.compile("com.netease.static.*").matcher(hostname).find();
    }
}
