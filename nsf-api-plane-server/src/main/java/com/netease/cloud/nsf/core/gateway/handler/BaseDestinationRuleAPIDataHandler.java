package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.API;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.netease.cloud.nsf.core.template.TemplateConst.DESTINATION_RULE_HOST;
import static com.netease.cloud.nsf.core.template.TemplateConst.DESTINATION_RULE_NAME;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public class BaseDestinationRuleAPIDataHandler extends APIDataHandler {

    private List<String> extraDestinations;

    public BaseDestinationRuleAPIDataHandler(List<String> extraDestinations) {
        this.extraDestinations = extraDestinations;
    }

    @Override
    List<TemplateParams> doHandle(TemplateParams baseParams, API api) {
        // 带porxyService的情况下，不需要创建destinationrule
        if (!CollectionUtils.isEmpty(api.getProxyServices())) return Collections.emptyList();
        Set<String> destinations = new HashSet(api.getProxyUris());
        // 根据插件中的目标服务 得到额外的destination rule
        if (!CollectionUtils.isEmpty(api.getPlugins())) {
            destinations.addAll(extraDestinations);
        }
        return destinations.stream()
                    .map(proxyUri -> TemplateParams.instance()
                                        .setParent(baseParams)
                                        .put(DESTINATION_RULE_HOST, proxyUri)
                                        .put(DESTINATION_RULE_NAME, proxyUri))
                    .collect(Collectors.toList());
    }
}
