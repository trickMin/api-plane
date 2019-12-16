package com.netease.cloud.nsf.core.gateway.service.impl;

import com.netease.cloud.nsf.core.envoy.EnvoyHttpClient;
import com.netease.cloud.nsf.core.istio.IstioHttpClient;
import com.netease.cloud.nsf.core.gateway.service.ResourceManager;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.ServiceAndPort;
import com.netease.cloud.nsf.meta.ServiceHealth;
import com.netease.cloud.nsf.meta.dto.ServiceAndPortDTO;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Value("${service.namespace.exclude}")
    private String excludeNamespace;

    private List<String> getExcludeNamespace() {
        List<String> ret = new ArrayList<>();
        if (StringUtils.isEmpty(excludeNamespace)) return ret;
        ret.addAll(Arrays.asList(excludeNamespace.split(",")));
        return ret;
    }

    @Override
    public List<Endpoint> getEndpointList() {
        Predicate<Endpoint> filter = endpoint ->
                endpoint.getHostname() != null &&
                        endpoint.getHostname() != null &&
                        endpoint.getPort() != null;
        for (String ns : getExcludeNamespace()) {
            filter = filter.and(endpoint -> !inNamespace(endpoint.getHostname(), ns));
        }

        return istioHttpClient.getEndpointList(filter);
    }

    @Override
    public List<Gateway> getGatewayList() {
        return istioHttpClient.getGatewayList(gateway ->
                // 包含gw_cluster label
                Objects.nonNull(gateway.getLabels()) &&
                        gateway.getLabels().containsKey("gw_cluster"));
    }

    @Override
    public List<String> getServiceList() {
        Predicate<Endpoint> filter = endpoint ->
                endpoint.getHostname() != null &&
                        !isServiceEntry(endpoint.getHostname());
        for (String ns : getExcludeNamespace()) {
            filter = filter.and(endpoint -> !inNamespace(endpoint.getHostname(), ns));
        }
        return istioHttpClient.getServiceList(filter);
    }


    public List<ServiceAndPort> getServiceAndPortList() {
        Map<String, Set<Integer>> servicePortMap = new LinkedHashMap<>();
        getEndpointList().forEach(endpoint -> {
                    if (!servicePortMap.containsKey(endpoint.getHostname())) {
                        servicePortMap.put(endpoint.getHostname(), new LinkedHashSet<>());
                    }
                    servicePortMap.get(endpoint.getHostname()).add(endpoint.getPort());
                }
        );
        return servicePortMap.entrySet().stream()
                .filter(entry -> !isServiceEntry(entry.getKey()))
                .map(entry -> {
                    ServiceAndPort sap = new ServiceAndPort();
                    sap.setName(entry.getKey());
                    sap.setPort(new ArrayList<>(entry.getValue()));
                    return sap;
                }).collect(Collectors.toList());
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
        }
        //todo: ports.size() > 1
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
        return inNamespace(hostName, "istio-system");
    }

    private boolean inKubeSystem(String hostName) {
        return inNamespace(hostName, "kube-system");
    }

    private boolean inGatewaySystem(String hostName) {
        return inNamespace(hostName, "gateway-system");
    }

    private boolean inNamespace(String hostName, String namespace) {
        String pattern = String.format("(.*)\\.%s\\.(.*)\\.(.*)\\.(.*)", namespace);
        return Pattern.compile(pattern).matcher(hostName).find();
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
