package com.netease.cloud.nsf.core.envoy;

import com.google.common.collect.ImmutableMap;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.meta.EndpointHealth;
import com.netease.cloud.nsf.meta.ServiceHealth;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.Pod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/28
 **/
@Component
public class EnvoyHttpClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KubernetesClient client;

    @Value(value = "${gatewayNamespace:gateway-system}")
    private String gatewayNamespace;

    @Value(value = "${gatewayNamespace:gateway-proxy}")
    private String gatewayName;

    private static final String GET_CLUSTER_HEALTH_JSON = "/clusters?format=json";

    private static final String SUBSET_PATTERN = ".+\\|\\d+\\|\\d+\\|.*";

    private String getEnvoyUrl() {
        //envoy service暂时未暴露管理端口，直接拿pod ip
        List<Pod> envoyPods = client.getObjectList(K8sResourceEnum.Pod.name(), gatewayNamespace, ImmutableMap.of("app", gatewayName));
        if (CollectionUtils.isEmpty(envoyPods)) throw new ApiPlaneException(ExceptionConst.ENVOY_POD_NON_EXIST);
        Optional<String> healthPod = envoyPods.stream()
                .filter(e -> e.getStatus().getPhase().equals("Running"))
                .map(e -> "http://" + e.getStatus().getPodIP())
                .findFirst();
        if (!healthPod.isPresent()) throw new ApiPlaneException(ExceptionConst.ENVOY_POD_NON_EXIST);
        //fixed port
        int port = 19000;
        return healthPod.get() + ":" + port;
    }

    public List<ServiceHealth> getServiceHealth(Function<String, String> nameHandler) {

        Map<String, List<EndpointHealth>> healthMap = new HashMap<>();
        String resp = restTemplate.getForObject(getEnvoyUrl() + GET_CLUSTER_HEALTH_JSON, String.class);
        ResourceGenerator rg = ResourceGenerator.newInstance(resp, ResourceType.JSON);

        List endpoints = rg.getValue("$.cluster_statuses[?(@.host_statuses)]");
        Function<String, String> nameFunction = nameHandler == null ? n -> n : nameHandler;
        if (CollectionUtils.isEmpty(endpoints)) return Collections.emptyList();
        endpoints.stream()
                .forEach(e -> {
                    ResourceGenerator gen = ResourceGenerator.newInstance(e, ResourceType.OBJECT);
                    EndpointHealth eh = new EndpointHealth();
                    String serviceName = gen.getValue("$.name");
                    //忽略subset
                    if (isSubset(serviceName)) return;
                    //不同端口的服务算一个服务，以后缀为服务名
                    String handledName = nameFunction.apply(serviceName);
                    List<String> addrs = gen.getValue("$..socket_address.address");
                    List<Integer> ports = gen.getValue("$..socket_address.port_value");
                    List<String> status = gen.getValue("$..health_status.eds_health_status");
                    if (CollectionUtils.isEmpty(addrs) || CollectionUtils.isEmpty(ports) || CollectionUtils.isEmpty(status)) return;
                    eh.setAddress(addrs.get(0) + ":" + ports.get(0));
                    eh.setPort(ports.get(0));
                    eh.setStatus(status.get(0));
                    healthMap.computeIfAbsent(handledName, v -> new ArrayList()).add(eh);
                });

        List<ServiceHealth> shs = new ArrayList<>();
        healthMap.forEach((name, ehs) -> {
            ServiceHealth sh = new ServiceHealth();
            sh.setName(name);
            sh.setEps(ehs);
            shs.add(sh);
        });
        return shs;
    }

    private boolean isSubset(String name) {
        return Pattern.matches(SUBSET_PATTERN, name);
    }

}

