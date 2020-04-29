package com.netease.cloud.nsf.mcp;

import com.netease.cloud.nsf.mcp.dao.StatusDao;
import com.netease.cloud.nsf.mcp.dao.meta.Status;
import com.netease.cloud.nsf.mcp.status.StatusConst;
import com.netease.cloud.nsf.meta.Gateway;
import com.netease.cloud.nsf.meta.ServiceHealth;
import com.netease.cloud.nsf.meta.dto.*;
import com.netease.cloud.nsf.service.GatewayService;
import com.netease.cloud.nsf.util.errorcode.ErrorCode;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/24
 **/
public class McpGatewayService implements GatewayService {

    private GatewayService innerService;
    private StatusDao statusDao;

    public McpGatewayService(GatewayService innerService, StatusDao statusDao) {
        this.innerService = innerService;
        this.statusDao = statusDao;
    }

    @Override
    @Transactional
    public void updateAPI(YxAPIDTO api) {
        innerService.updateAPI(api);
        notifyStatus();
    }

    @Override
    @Transactional
    public void deleteAPI(YxAPIDTO api) {
        innerService.deleteAPI(api);
        notifyStatus();
    }

    @Override
    @Transactional
    public void updateAPI(PortalAPIDTO portalAPI) {
        innerService.updateAPI(portalAPI);
        notifyStatus();
    }

    @Override
    @Transactional
    public void deleteAPI(PortalAPIDeleteDTO portalAPI) {
        innerService.deleteAPI(portalAPI);
        notifyStatus();
    }

    @Override
    @Transactional
    public void updateService(PortalServiceDTO service) {
        innerService.updateService(service);
        notifyStatus();
    }

    @Override
    public ErrorCode checkUpdateService(PortalServiceDTO service) {
        return innerService.checkUpdateService(service);
    }

    @Override
    @Transactional
    public void deleteService(PortalServiceDTO service) {
        innerService.deleteService(service);
        notifyStatus();
    }

    @Override
    public PluginOrderDTO getPluginOrder(PluginOrderDTO pluginOrderDto) {
        return innerService.getPluginOrder(pluginOrderDto);
    }

    @Override
    @Transactional
    public void updatePluginOrder(PluginOrderDTO pluginOrderDto) {
        innerService.updatePluginOrder(pluginOrderDto);
        notifyStatus();
    }

    @Override
    @Transactional
    public void deletePluginOrder(PluginOrderDTO pluginOrderDTO) {
        innerService.deletePluginOrder(pluginOrderDTO);
        notifyStatus();
    }

    @Override
    public List<String> getServiceList() {
        return innerService.getServiceList();
    }

    @Override
    public List<ServiceAndPortDTO> getServiceAndPortList(String name, String type, String registryId) {
        return innerService.getServiceAndPortList(name, type, registryId);
    }

    @Override
    public List<Gateway> getGatewayList() {
        return innerService.getGatewayList();
    }

    @Override
    public List<ServiceHealth> getServiceHealthList(String host) {
        return innerService.getServiceHealthList(host);
    }

    @Override
    @Transactional
    public void updateIstioGateway(PortalIstioGatewayDTO portalGateway) {
        innerService.updateIstioGateway(portalGateway);
        notifyStatus();
    }

    @Override
    public PortalIstioGatewayDTO getIstioGateway(String clusterName) {
        return innerService.getIstioGateway(clusterName);
    }

    @Override
    @Transactional
    public void updateGlobalPlugins(GlobalPluginDTO globalPluginsDTO) {
        innerService.updateGlobalPlugins(globalPluginsDTO);
        notifyStatus();
    }

    @Override
    @Transactional
    public void deleteGlobalPlugins(GlobalPluginsDeleteDTO globalPluginsDeleteDTO) {
        innerService.deleteGlobalPlugins(globalPluginsDeleteDTO);
        notifyStatus();
    }

    private void notifyStatus() {
        statusDao.update(new Status(StatusConst.RESOURCES_VERSION, new Date().toString()));
    }
}
