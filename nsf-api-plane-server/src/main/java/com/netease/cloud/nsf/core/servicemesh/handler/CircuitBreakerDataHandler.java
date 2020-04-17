package com.netease.cloud.nsf.core.servicemesh.handler;

import com.netease.cloud.nsf.core.gateway.handler.DataHandler;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.ServiceMeshCircuitBreaker;

import java.util.*;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;
import static com.netease.cloud.nsf.core.template.TemplateConst.GATEWAY_PLUGIN_ROUTES;

public class CircuitBreakerDataHandler implements DataHandler<ServiceMeshCircuitBreaker> {

    private List<String> extraCircuitBreaker;

    public CircuitBreakerDataHandler(List<String> extraCircuitBreaker) {
        this.extraCircuitBreaker = extraCircuitBreaker;
    }

    @Override
    public List<TemplateParams> handle(ServiceMeshCircuitBreaker serviceMeshCircuitBreaker) {
        String circuitBreakerPluginName = serviceMeshCircuitBreaker.getHost() + "." + serviceMeshCircuitBreaker.getRouterName();
        TemplateParams tp = TemplateParams.instance()
                .put(GATEWAY_PLUGIN_NAME, circuitBreakerPluginName)
                .put(GATEWAY_PLUGIN_HOSTS, Arrays.asList(serviceMeshCircuitBreaker.getHost()))
                .put(GATEWAY_PLUGIN_ROUTES, Arrays.asList(serviceMeshCircuitBreaker.getRouterName()))
                .put(GATEWAY_PLUGIN_NAMESPACE, serviceMeshCircuitBreaker.getNamespace());
        return doHandle(tp, serviceMeshCircuitBreaker);

    }

    private List<TemplateParams> doHandle(TemplateParams tp, ServiceMeshCircuitBreaker circuitBreaker) {

        if (circuitBreaker == null) return Collections.EMPTY_LIST;

        List<TemplateParams> params = new ArrayList<>();
        List<String> plugins = extraCircuitBreaker;
        TemplateParams pmParams = TemplateParams.instance()
                .setParent(tp)
                .put(GATEWAY_PLUGIN_PLUGINS, plugins);
        params.add(pmParams);
        return params;
    }
}
