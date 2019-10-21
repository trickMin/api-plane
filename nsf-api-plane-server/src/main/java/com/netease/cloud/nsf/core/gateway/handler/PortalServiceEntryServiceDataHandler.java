package com.netease.cloud.nsf.core.gateway.handler;

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
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/10/8
 **/
public class PortalServiceEntryServiceDataHandler extends ServiceDataHandler {

    @Override
    List<TemplateParams> doHandle(TemplateParams tp, Service service) {

        TemplateParams params = TemplateParams.instance()
                .put(NAMESPACE, service.getNamespace());

        // host由服务类型决定，
        // 当为静态时，后端地址为IP或域名，使用服务的code作为host

        if (Const.PROXY_SERVICE_TYPE_STATIC.equals(service.getType())) {

            String host = decorateHost(service.getCode());
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
        }
        return Arrays.asList(params);
    }
}