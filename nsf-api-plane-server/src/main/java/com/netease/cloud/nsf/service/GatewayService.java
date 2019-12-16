package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.PodVersion;
import com.netease.cloud.nsf.meta.PodStatus;
import com.netease.cloud.nsf.meta.SidecarVersionManagement;
import com.netease.cloud.nsf.meta.ServiceHealth;
import com.netease.cloud.nsf.meta.dto.*;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public interface GatewayService {

    void updateAPI(YxAPIDTO api);

    void deleteAPI(YxAPIDTO api);

    void updateAPI(PortalAPIDTO portalAPI);

    void deleteAPI(PortalAPIDTO portalAPI);

    void updateService(PortalServiceDTO service);

    void deleteService(PortalServiceDTO service);

    PluginOrderDTO getPluginOrder();

    void updatePluginOrder(PluginOrderDTO pluginOrderDto);

    void deletePluginOrder(PluginOrderDTO pluginOrderDTO);

    List<String> getServiceList();

    List<ServiceAndPortDTO> getServiceAndPortList(String name);

    List<Gateway> getGatewayList();

    List<ServiceHealth> getServiceHealthList(String host);

    void updateSVM(SidecarVersionManagement svm);

    List<PodStatus> queryByPodNameList(PodVersion podVersion);
}
