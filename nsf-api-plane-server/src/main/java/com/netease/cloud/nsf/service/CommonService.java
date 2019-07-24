package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.GatewaySync;
import com.netease.cloud.nsf.meta.PluginTemplate;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/23
 **/
public interface CommonService {

    List<String> getServiceList();

    List<Gateway> getGatewayList();

    List<PluginTemplate> getPluginList();

    List<GatewaySync> getGatewaySyncList();
}
