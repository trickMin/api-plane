package org.hango.cloud.service;

import org.hango.cloud.meta.ServiceHealth;
import org.hango.cloud.meta.dto.*;
import org.hango.cloud.util.errorcode.ErrorCode;

import java.util.List;
import java.util.Map;


public interface GatewayService {

    void updateAPI(PortalAPIDTO portalAPI);

    void deleteAPI(PortalAPIDeleteDTO portalAPI);

    void updateGatewayPlugin(GatewayPluginDTO plugin);

    void deleteGatewayPlugin(GatewayPluginDTO plugin);

    void updateService(PortalServiceDTO service);

    /**
     * 用于服务发布时参数校验
     *
     * @param service
     * @return
     */
    ErrorCode checkUpdateService(PortalServiceDTO service);

    void deleteService(PortalServiceDTO service);

    PluginOrderDTO getPluginOrder(PluginOrderDTO pluginOrderDto);

    void updatePluginOrder(PluginOrderDTO pluginOrderDto);

    void deletePluginOrder(PluginOrderDTO pluginOrderDTO);

    void deleteEnvoyFilter(EnvoyFilterDTO envoyFilterOrder);

    List<String> getServiceList();

    List<ServiceAndPortDTO> getServiceAndPortList(String name, String type, String registryId, Map<String, String> filters);


    List<ServiceHealth> getServiceHealthList(String host, List<String> subsets, String gateway);

    void updateIstioGateway(PortalIstioGatewayDTO portalGateway);

    PortalIstioGatewayDTO getIstioGateway(String clusterName);

    /**
     * 获取Dubbo Meta元数据信息
     *
     * @param igv             接口+版本+分组 {interface:group:version}
     * @return
     */
    List<DubboMetaDto> getDubboMeta(String igv);

    void updateEnvoyFilter(EnvoyFilterDTO grpcEnvoyFilterDTO);

    void updateGrpcEnvoyFilter(GrpcEnvoyFilterDto grpcEnvoyFilterDto);

    void deleteGrpcEnvoyFilter(GrpcEnvoyFilterDto grpcEnvoyFilterDto);

    void updateSecret(PortalSecretDTO portalSecretDTO);

    void deleteSecret(PortalSecretDTO portalSecretDTO);
}
