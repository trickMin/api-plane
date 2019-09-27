package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.gateway.processor.ModelProcessor;
import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.Endpoint;
import com.netease.cloud.nsf.meta.Service;
import com.netease.cloud.nsf.util.CommonUtil;
import com.netease.cloud.nsf.util.Const;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class PortalDestinationRuleServiceDataHandler extends ServiceDataHandler {


    private static final String serviceServiceEntry = "gateway/service/serviceEntry";

    private ModelProcessor subModelProcessor;

    public PortalDestinationRuleServiceDataHandler(ModelProcessor subModelProcessor) {
        this.subModelProcessor = subModelProcessor;
    }

    @Override
    List<TemplateParams> doHandle(TemplateParams tp, Service service) {
        List<String> destinations = new ArrayList<>();
        TemplateParams params = TemplateParams.instance()
                .put(DESTINATION_RULE_NAME, service.getCode().toLowerCase())
                .put(DESTINATION_RULE_HOST, service.getBackendService())
                .put(NAMESPACE, service.getNamespace())
                .put(API_SERVICE, service.getCode())
                .put(API_GATEWAY, service.getGateway());

        // host由服务类型决定，
        // 1. 当为动态时，则直接使用服务的后端地址，一般为httpbin.default.svc类似，
        // 2. 当为静态时，后端地址为IP或域名，使用服务的code作为host

        String host = service.getBackendService();
        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {

            host = decorateHost(service.getCode());
            String backendService = service.getBackendService();

            List<String> addrs = new ArrayList<>();
            List<Endpoint> endpoints = new ArrayList<>();
            if (backendService.contains(",")) {
                addrs.addAll(Arrays.asList(backendService.split(",")));
            } else {
                addrs.add(backendService);
            }

            addrs.stream()
                    .forEach(addr -> {
                        Endpoint e = new Endpoint();
                        if (CommonUtil.isValidIPPortAddr(addr)) {
                            String[] ipPort = addr.split(":");
                            e.setAddress(ipPort[0]);
                            e.setPort(Integer.valueOf(ipPort[1]));
                        } else {
                            e.setAddress(addr);
                        }
                        endpoints.add(e);
                    });

            params.put("endpoints", endpoints)
                    .put(SERVICE_ENTRY_NAME, service.getCode().toLowerCase())
                    .put(SERVICE_ENTRY_HOST, host);
            destinations.add(subModelProcessor.process(serviceServiceEntry, params));
        }
        params.put(DESTINATION_RULE_HOST, host);
        return Arrays.asList(params);
    }

    private String decorateHost(String code) {
        return String.format("com.netease.%s", code);
    }
}
