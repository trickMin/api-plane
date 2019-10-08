package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.Service;

import java.util.Arrays;
import java.util.List;

import static com.netease.cloud.nsf.core.template.TemplateConst.*;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class PortalDestinationRuleServiceDataHandler extends ServiceDataHandler {

    @Override
    List<TemplateParams> doHandle(TemplateParams tp, Service service) {
        TemplateParams params = TemplateParams.instance()
                .put(DESTINATION_RULE_NAME, service.getCode().toLowerCase())
                .put(DESTINATION_RULE_HOST, service.getBackendService())
                .put(NAMESPACE, service.getNamespace())
                .put(API_SERVICE, service.getCode())
                .put(API_GATEWAY, service.getGateway());

        // host由服务类型决定，
        // 当为动态时，则直接使用服务的后端地址，一般为httpbin.default.svc类似
        params.put(DESTINATION_RULE_HOST, decorateHost(service.getCode()));
        return Arrays.asList(params);
    }

}
