package com.netease.cloud.nsf.core.servicemesh;

import com.netease.cloud.nsf.core.AbstractConfigManagerSupport;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.K8sResourcePack;
import com.netease.cloud.nsf.core.k8s.operator.VersionManagerOperator;
import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.meta.ServiceMeshRateLimit;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import com.netease.cloud.nsf.service.PluginService;
import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.networking.v1alpha3.VersionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
@Component
public class K8sServiceMeshConfigManager extends AbstractConfigManagerSupport implements ServiceMeshConfigManager {

    ServiceMeshIstioModelEngine modelEngine;
    MultiK8sConfigStore multiK8sConfigStore;
    PluginService pluginService;

    private static final String VM_RESOURCE_NAME = "version-manager";

    @Autowired
    public K8sServiceMeshConfigManager(ServiceMeshIstioModelEngine modelEngine, MultiK8sConfigStore multiK8sConfigStore, PluginService pluginService) {
        this.modelEngine = modelEngine;
        this.multiK8sConfigStore = multiK8sConfigStore;
        this.pluginService = pluginService;
    }

    @Override
    public void updateConfig(SidecarVersionManagement svm) {
        List<K8sResourcePack> resources = modelEngine.translate(svm);
        update(multiK8sConfigStore, resources, modelEngine);
    }

    @Override
    public List<PodStatus> querySVMConfig(PodVersion podVersion) {
        String clusterId = podVersion.getClusterId();
        HasMetadata versionmanager = multiK8sConfigStore.get(K8sResourceEnum.VersionManager.name(), podVersion.getNamespace(), VM_RESOURCE_NAME, clusterId);
        if (versionmanager == null) {
            return null;
        }
        VersionManagerOperator ir = (VersionManagerOperator) modelEngine.getOperator().resolve(versionmanager);
        return ir.getPodVersion(podVersion, (VersionManager) versionmanager);
    }

    @Override
    public String querySVMExpectedVersion(String clusterId, String namespace, String workLoadType, String workLoadName) {
        HasMetadata versionmanager = multiK8sConfigStore.get(K8sResourceEnum.VersionManager.name(), namespace, VM_RESOURCE_NAME, clusterId);
        if(versionmanager == null) {
            return null;
        }
        VersionManagerOperator ir = (VersionManagerOperator) modelEngine.getOperator().resolve(versionmanager);
        return ir.getExpectedVersion((VersionManager)versionmanager, workLoadType, workLoadName);
    }

    @Override
    public void updateRateLimit(ServiceMeshRateLimit rateLimit) {
        List<K8sResourcePack> packs = modelEngine.translate(rateLimit);
        update(multiK8sConfigStore, packs, modelEngine);
    }

}
