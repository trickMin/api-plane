package com.netease.cloud.nsf.core.gateway.handler;

import com.netease.cloud.nsf.core.template.TemplateParams;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;

import java.util.Arrays;
import java.util.List;

import static com.netease.cloud.nsf.core.template.TemplateConst.NAMESPACE;
import static com.netease.cloud.nsf.core.template.TemplateConst.VERSION_MANAGER_WORKLOADS;

public class VersionManagersDataHandler implements DataHandler<SidecarVersionManagement> {

    @Override
    public List<TemplateParams> handle(SidecarVersionManagement svm) {

        TemplateParams pmParams = TemplateParams.instance()
                .put(NAMESPACE, svm.getNamespace())
                .put(VERSION_MANAGER_WORKLOADS, svm.getWorkLoads());

        return Arrays.asList(pmParams);
    }
}
