package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;

import java.util.List;

public interface VersionManagerService {

    void updateSVM(SidecarVersionManagement svm);

    List<PodStatus> queryByPodNameList(PodVersion podVersion);
}
