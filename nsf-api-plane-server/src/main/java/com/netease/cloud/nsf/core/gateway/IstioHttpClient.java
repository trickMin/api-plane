package com.netease.cloud.nsf.core.gateway;

import com.netease.cloud.nsf.core.editor.EditorContext;
import com.netease.cloud.nsf.core.editor.ResourceGenerator;
import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.k8s.KubernetesClient;
import com.netease.cloud.nsf.util.K8sResourceEnum;
import com.netease.cloud.nsf.util.PathExpressionEnum;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import com.netease.cloud.nsf.util.exception.ExceptionConst;
import io.fabric8.kubernetes.api.model.Pod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
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

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private KubernetesClient client;

    @Autowired
    private EditorContext editorContext;

    private String getIstioUrl() {
        String url = client.getUrl(K8sResourceEnum.Pod.name(), NAMESPACE) + "?labelSelector=app%3D" + NAME;
        List<Pod> istioPods = client.getObjectList(url);
        if (CollectionUtils.isEmpty(istioPods)) throw new ApiPlaneException(ExceptionConst.ISTIO_POD_NON_EXIST);
        Pod istioPod = istioPods.get(0);
        String ip = istioPod.getStatus().getPodIP();
        //FIXME 暂时写死
        String port = "8080";
        return String.format("http://%s:%s", ip, port);
    }

    public List<String> getServiceNameList() {
        ResponseEntity response = restTemplate.getForEntity(getIstioUrl() + GET_ENDPOINTZ_PATH, String.class);
        Set<String> svcs = new HashSet<>(ResourceGenerator.newInstance(response.getBody(), ResourceType.JSON, editorContext).getValue(PathExpressionEnum.ISTIO_GET_SVC.translate()));
        return svcs.stream().filter(svc -> {
            Matcher matcher = Pattern.compile("(.*)\\.(.*)\\.(.*)\\.(.*)\\.(.*)").matcher(svc);
            if (matcher.find()) {
                return !"istio-system".equals(matcher.group(2));
            }
            return true;
        }).collect(Collectors.toList());
    }
}


