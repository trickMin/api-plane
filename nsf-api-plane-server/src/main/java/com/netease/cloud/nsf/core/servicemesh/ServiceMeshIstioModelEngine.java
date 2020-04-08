package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.IstioModelEngine;
import com.netease.cloud.nsf.core.gateway.handler.VersionManagersDataHandler;
import com.netease.cloud.nsf.core.gateway.processor.DefaultModelProcessor;
import com.netease.cloud.nsf.core.k8s.K8sResourcePack;
import com.netease.cloud.nsf.core.k8s.operator.IntegratedResourceOperator;
import com.netease.cloud.nsf.core.template.TemplateTranslator;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
@Component
public class ServiceMeshIstioModelEngine extends IstioModelEngine {

    DefaultModelProcessor defaultModelProcessor;
    TemplateTranslator templateTranslator;

    private static final String versionManager = "sidecarVersionManagement";

    @Autowired
    public ServiceMeshIstioModelEngine(IntegratedResourceOperator operator, TemplateTranslator templateTranslator) {
        super(operator);
        this.templateTranslator = templateTranslator;
        this.defaultModelProcessor = new DefaultModelProcessor(templateTranslator);
    }

    public List<K8sResourcePack> translate(SidecarVersionManagement svm) {
        List<K8sResourcePack> resources = new ArrayList<>();
        List<String> versionManagers = defaultModelProcessor.process(versionManager, svm, new VersionManagersDataHandler());
        resources.addAll(generateK8sPack(Arrays.asList(versionManagers.get(0))));
        return resources;
    }
}
