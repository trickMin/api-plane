package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.Service;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/27
 **/
public abstract class ServiceDataHandler implements DataHandler<Service> {

    @Override
    public List<TemplateParams> handle(Service service) {
        return doHandle(TemplateParams.instance(), service);
    }

    abstract List<TemplateParams> doHandle(TemplateParams tp, Service service);

    String decorateHost(String code) {
        return String.format("com.netease.%s", code);
    }
}
