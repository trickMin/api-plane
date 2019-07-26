package com.netease.cloud.nsf.core.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Component
public class IstioClient {

    private static final String NAMESPACE = "istio-system";
    private static final String NAME = "pilot";

    private static final String GET_EDSZ_PATH = "/debug/edsz";

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private KubernetesClient client;

    private String getIstioUrl() {
        List<Pod> istioPods = client.pods().inNamespace(NAMESPACE).withLabel("app", NAME).list().getItems();
        if (CollectionUtils.isEmpty(istioPods)) throw new ApiPlaneException(ExceptionConst.ISTIO_POD_NON_EXIST);
        Pod istioPod = istioPods.get(0);
        String ip = istioPod.getStatus().getPodIP();
        //FIXME 暂时写死
        String port = "8080";
        return String.format("http://%s:%s", ip, port);
    }

    public List<String> getServiceNameList() {
        Endpoint[] edsz = restTemplate.getForObject(getIstioUrl() + GET_EDSZ_PATH, Endpoint[].class);

        if (edsz == null || edsz.length == 0) return Collections.emptyList();
        return Arrays.stream(edsz)
                .map(e -> {
                    String clusterName = e.getClusterName();
                    if (clusterName.contains("||")) {
                        return clusterName.split("||")[1];
                    }
                    return clusterName;
                })
                .distinct()
                .collect(Collectors.toList());
    }

}


