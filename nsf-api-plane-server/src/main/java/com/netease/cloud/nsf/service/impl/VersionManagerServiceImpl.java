package com.netease.cloud.nsf.service.impl;


import com.netease.cloud.nsf.core.servicemesh.ServiceMeshConfigManager;
import com.netease.cloud.nsf.meta.IptablesConfig;
import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import com.netease.cloud.nsf.service.VersionManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2020/4/8
 **/
@Service
public class VersionManagerServiceImpl implements VersionManagerService {

    private ServiceMeshConfigManager configManager;

    private static final Logger log = LoggerFactory.getLogger(VersionManagerServiceImpl.class);

    @Value("${crdUpdateRetryCount:3}")
    private int RETRY_COUNT;

    @Autowired
    public VersionManagerServiceImpl(ServiceMeshConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void updateSVM(SidecarVersionManagement svm) {
        synchronized (this) {
            retryUpdateSVM(svm);
        }
    }

    @Override
    public List<PodStatus> queryByPodNameList(PodVersion podVersion) {
        return configManager.querySVMConfig(podVersion);
    }

    @Override
    public IptablesConfig queryIptablesConfigByApp(String clusterId, String namespace, String appName) {
        return configManager.queryIptablesConfigByApp(clusterId, namespace, appName);
    }

    private void retryUpdateSVM (SidecarVersionManagement svm){

        int call = 0;
        boolean completed = false;
        while (!completed) {
            try {
                configManager.updateConfig(svm);
                completed = true;
            } catch (Exception e) {
                log.warn("update SVM crd error,retry request",e);
                if (call >= RETRY_COUNT) {
                    throw e;
                } else {
                    call ++ ;
                    continue;
                }
            }
        }


    }
}
