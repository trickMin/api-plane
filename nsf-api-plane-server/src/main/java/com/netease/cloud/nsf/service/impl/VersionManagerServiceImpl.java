package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.servicemesh.ServiceMeshConfigManager;
import com.netease.cloud.nsf.meta.IptablesConfig;
import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import com.netease.cloud.nsf.service.VersionManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
@Service
public class VersionManagerServiceImpl implements VersionManagerService {

    private ServiceMeshConfigManager configManager;

    @Autowired
    public VersionManagerServiceImpl(ServiceMeshConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void updateSVM(SidecarVersionManagement svm) {
        configManager.updateConfig(svm, svm.getClusterId());
    }

    @Override
    public List<PodStatus> queryByPodNameList(PodVersion podVersion) {
        return configManager.querySVMConfig(podVersion, podVersion.getClusterId());
    }

    @Override
    public IptablesConfig queryIptablesConfigByApp(String clusterId, String namespace, String appName) {
        return configManager.queryIptablesConfigByApp(clusterId, namespace, appName);
    }
}
