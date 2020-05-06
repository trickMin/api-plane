package com.netease.cloud.nsf.core.kiali;

import com.netease.cloud.nsf.meta.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/12
 **/
@Component
public class KialiHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(KialiHttpClient.class);


    @Autowired
    private RestTemplate restTemplate;

    @Value(value = "${kialiNamespace:istio-system}")
    private String kialiNamespace;

    @Value(value = "${kialiName:kiali}")
    private String kialiName;

    @Value(value = "${kialiUrl:#{null}}")
    private String kialiUrl;

    private static final String GRAPH_QUERY_URI = "/kiali/api/namespaces/graph?duration=%s&graphType=%s&injectServiceNodes=%b" +
            "&groupBy=app&appenders=deadNode,sidecarsCheck,serviceEntry,istio&namespaces=%s";

    private String getKialiUrl() {
        if (!StringUtils.isEmpty(kialiUrl)) return kialiUrl;
        //fixed port
        int port = 20001;
        String svcName = kialiName + "." + kialiNamespace;
        return String.format("http://%s:%d", svcName, port);
    }

    public Graph getGraph(String namespaces, String graphType, String duration, boolean injectServices) {

        Graph resp = restTemplate.getForObject(getKialiUrl() + String.format(GRAPH_QUERY_URI, duration, graphType, injectServices, namespaces),
                Graph.class);

        return resp;
    }

    public HttpEntity<String> redirectToKiali(HttpMethod method, String path, String body) {
        return restTemplate.exchange(new RequestEntity<>(body, method, URI.create(getKialiUrl() + path)), String.class);
    }
}
