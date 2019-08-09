package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.Pod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/25
 **/
@Component
public class IstioHttpClient {

    private static final String NAMESPACE = "istio-system";
    private static final String NAME = "pilot";

    private static final String GET_ENDPOINTZ_PATH = "/debug/endpointz";

    @Value(value = "${istioHttpUrl:#{null}}")
    private String istioHttpUrl;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private KubernetesClient client;

    @Autowired
    private EditorContext editorContext;

    private String getIstioUrl() {
        if (!StringUtils.isEmpty(istioHttpUrl)) return istioHttpUrl;
        String url = client.getUrl(K8sResourceEnum.Pod.name(), NAMESPACE) + "?labelSelector=app%3D" + NAME;
        List<Pod> istioPods = client.getObjectList(url);
        if (CollectionUtils.isEmpty(istioPods)) throw new ApiPlaneException(ExceptionConst.ISTIO_POD_NON_EXIST);
        Pod istioPod = istioPods.get(0);
        String ip = istioPod.getStatus().getPodIP();
        //FIXME 暂时写死
        String port = "8080";
        return String.format("http://%s:%s", ip, port);
    }

    public List<Endpoint> getEndpointList() {
        List<Endpoint> endpoints = new ArrayList<>();
        ResponseEntity response = restTemplate.getForEntity(getIstioUrl() + GET_ENDPOINTZ_PATH, String.class);
        List svcs = ResourceGenerator.newInstance(response.getBody(), ResourceType.JSON, editorContext).getValue(PathExpressionEnum.ISTIO_GET_SVC.translate());
        svcs.stream().forEach(
                svc -> {
                    Endpoint endpoint = new Endpoint();
                    ResourceGenerator gen = ResourceGenerator.newInstance(svc, ResourceType.OBJECT, editorContext);
                    endpoint.setHostname(gen.getValue("$.service.hostname"));
                    endpoint.setAddress(gen.getValue("$.endpoint.Address"));
                    endpoint.setPort(gen.getValue("$.endpoint.ServicePort.port"));
                    endpoint.setLabels(gen.getValue("$.labels"));
                    endpoints.add(endpoint);
                }
        );
        return endpoints.stream()
                .filter(endpoint -> endpoint.getAddress() != null && endpoint.getHostname() != null && endpoint.getPort() != null)
                .filter(endpoint -> !Pattern.compile("(.*)\\.istio-system\\.(.*)\\.(.*)\\.(.*)").matcher(endpoint.getHostname()).find())
                .distinct().collect(Collectors.toList());
    }

    public List<Gateway> getGatewayList() {
        List<Gateway> gateways = new ArrayList<>();
        ResponseEntity response = restTemplate.getForEntity(getIstioUrl() + GET_ENDPOINTZ_PATH, String.class);
        List svcs = ResourceGenerator.newInstance(response.getBody(), ResourceType.JSON, editorContext).getValue(PathExpressionEnum.ISTIO_GET_GATEWAY.translate("gateway-proxy.*"));
        svcs.stream().forEach(svc -> {
            Gateway gateway = new Gateway();
            ResourceGenerator gen = ResourceGenerator.newInstance(svc, ResourceType.OBJECT, editorContext);
            gateway.setHostname(gen.getValue("$.service.hostname"));
            gateway.setAddress(gen.getValue("$.endpoint.Address"));
            gateway.setLabels(gen.getValue("$.labels"));
            gateways.add(gateway);
        });
        return gateways.stream().filter(gateway -> gateway.getHostname() != null && gateway.getAddress() != null).distinct().collect(Collectors.toList());
    }
}

