package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.Service;
import com.netease.cloud.nsf.util.Const;

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
                .put(DESTINATION_RULE_NAME, service.getCode())
                .put(DESTINATION_RULE_HOST, service.getBackendService())
                .put(NAMESPACE, service.getNamespace())
                .put(API_SERVICE, service.getCode())
                .put(DESTINATION_RULE_CONSECUTIVE_ERRORS, service.getConsecutiveErrors())
                .put(DESTINATION_RULE_BASE_EJECTION_TIME, service.getBaseEjectionTime())
                .put(DESTINATION_RULE_MAX_EJECTION_PERCENT, service.getMaxEjectionPercent())
                .put(DESTINATION_RULE_PATH, service.getPath())
                .put(DESTINATION_RULE_TIMEOUT, service.getTimeout())
                .put(DESTINATION_RULE_EXPECTED_STATUSES, service.getExpectedStatuses())
                .put(DESTINATION_RULE_HEALTHY_INTERVAL, service.getHealthyInterval())
                .put(DESTINATION_RULE_HEALTHY_THRESHOLD, service.getHealthyThreshold())
                .put(DESTINATION_RULE_UNHEALTHY_INTERVAL, service.getUnhealthyInterval())
                .put(DESTINATION_RULE_UNHEALTHY_THRESHOLD, service.getUnhealthyThreshold())
                .put(DESTINATION_RULE_ALT_STAT_NAME, service.getServiceTag())
                .put(DESTINATION_RULE_LOAD_BALANCER, service.getLoadBalancer())
                .put(API_GATEWAY, service.getGateway());

        // host由服务类型决定，
        // 当为动态时，则直接使用服务的后端地址，一般为httpbin.default.svc类似
        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {
            params.put(DESTINATION_RULE_HOST, decorateHost(service.getCode()));
        }
        return Arrays.asList(params);
    }

}
