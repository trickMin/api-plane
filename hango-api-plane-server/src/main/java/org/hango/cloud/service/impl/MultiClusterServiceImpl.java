package org.hango.cloud.service.impl;

import io.fabric8.kubernetes.api.model.HasMetadata;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.networking.v1alpha3.Endpoint;
import me.snowdrop.istio.api.networking.v1alpha3.ServiceEntrySpec;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.cache.K8sResourceCache;
import org.hango.cloud.core.GlobalConfig;
import org.hango.cloud.core.gateway.service.impl.K8sConfigStore;
import org.hango.cloud.core.k8s.K8sResourceApiEnum;
import org.hango.cloud.core.k8s.MultiClusterK8sClient;
import org.hango.cloud.meta.dto.ApiPlaneResult;
import org.hango.cloud.meta.dto.DataCorrectResultDTO;
import org.hango.cloud.meta.dto.ResourceCheckDTO;
import org.hango.cloud.meta.dto.ResourceDTO;
import org.hango.cloud.service.MultiClusterService;
import org.hango.cloud.util.GPortalHttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:04
 **/
@Service
public class MultiClusterServiceImpl implements MultiClusterService {
    private static final Logger logger = LoggerFactory.getLogger(MultiClusterServiceImpl.class);

    @Autowired
    private K8sConfigStore k8sConfigStore;

    @Autowired
    private K8sResourceCache k8sResourceCache;

    @Autowired
    private MultiClusterK8sClient multiClusterK8sClient;

    @Autowired
    private GlobalConfig globalConfig;

    @Autowired
    private GPortalHttpUtil gPortalHttpUtil;


    @Value(value = "${gportal.gwClusterName}")
    private String gwClusterName;


    public final static String DESTINATION_RULE = K8sResourceApiEnum.DestinationRule.name();

    public final static String VIRTUAL_SERVICE = K8sResourceApiEnum.VirtualService.name();

    public final static String SERVICE_ENTRY = K8sResourceApiEnum.ServiceEntry.name();

//    public final static String GATEWAY_PLUGIN = "GatewayPlugin";

    private final static String GW_CLUSTER = "gw_cluster";

    private final static String DATA_VERSION = "skiff-nsf-data-version";

    private final static String PUBLISHED_RESOURCE_INFO = "publishedResourceInfo";


    @Override
    public ApiPlaneResult<Map<String, List<ResourceCheckDTO>>> dataCheck() {
        //获取db资源
        ApiPlaneResult<Map<String, List<ResourceDTO>>> httpResult = gPortalHttpUtil.getResourceInfoFromDB();
        if (httpResult.isFailed()){
            return ApiPlaneResult.ofFailed(httpResult.getErrorCode(), httpResult.getErrorMsg());
        }
        Map<String, List<ResourceDTO>> dbResource = httpResult.getData();
        //服务版本号校验
        List<ResourceCheckDTO> drCheckDTO = checkResourceInfo(dbResource.get(DESTINATION_RULE), getResourceDTO(DESTINATION_RULE), ResourceDTO::getResourceVersion);
        //路由版本号校验
        List<ResourceCheckDTO> vsCheckDTO = checkResourceInfo(dbResource.get(VIRTUAL_SERVICE), getResourceDTO(VIRTUAL_SERVICE),ResourceDTO::getResourceVersion);
//        //全局插件版本号校验
//        List<ResourceCheckDTO> gpCheckDTO = checkResourceInfo(dbResource.get(GATEWAY_PLUGIN), getCustomResource(GATEWAY_PLUGIN), ResourceDTO::getResourceVersion);
        //静态地址校验
        List<ResourceCheckDTO> seCheckDTOS = checkServiceEntry(dbResource.get(DESTINATION_RULE));
        Map<String, List<ResourceCheckDTO>> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(drCheckDTO)){
            result.put(DESTINATION_RULE, drCheckDTO);
        }
        if (CollectionUtils.isNotEmpty(vsCheckDTO)){
            result.put(VIRTUAL_SERVICE, vsCheckDTO);
        }
//        if (CollectionUtils.isNotEmpty(gpCheckDTO)){
//            result.put(GATEWAY_PLUGIN, gpCheckDTO);
//        }
        if (CollectionUtils.isNotEmpty(seCheckDTOS)){
            result.put(SERVICE_ENTRY, seCheckDTOS);
        }
        return ApiPlaneResult.ofSuccess(result);
    }

    @Override
    public Map<String, DataCorrectResultDTO> dataCorrection(Map<String, List<ResourceCheckDTO>> param) {
        Map<String, DataCorrectResultDTO> result = new HashMap<>();
        if (param.containsKey(DESTINATION_RULE)){
            DataCorrectResultDTO drCorrectResult = correct(param.get(DESTINATION_RULE), DESTINATION_RULE);
            result.put(DESTINATION_RULE, drCorrectResult);
        }
        if (param.containsKey(VIRTUAL_SERVICE)){
            DataCorrectResultDTO vsCorrectResult = correct(param.get(VIRTUAL_SERVICE), VIRTUAL_SERVICE);
            result.put(VIRTUAL_SERVICE, vsCorrectResult);
        }
//        if (param.containsKey(GATEWAY_PLUGIN)){
//            DataCorrectResultDTO gpCorrectResult = correct(param.get(GATEWAY_PLUGIN), GATEWAY_PLUGIN);
//            result.put(GATEWAY_PLUGIN, gpCorrectResult);
//        }
        if (param.containsKey(SERVICE_ENTRY)){
            DataCorrectResultDTO seCorrectResult = correct(param.get(SERVICE_ENTRY), SERVICE_ENTRY);
            result.put(SERVICE_ENTRY, seCorrectResult);
        }
        return result;
    }



    private DataCorrectResultDTO correct(List<ResourceCheckDTO> resourceCheckDTOS, String kind){
        /**
         * 通过重新发布操作更新配置
         * 特别说明：补偿任务不执行删除CR的操作，只进行告警
         */
        List<Long> ids = resourceCheckDTOS.stream()
                .filter(o -> o.getDbResourceInfo() != null)
                .map(ResourceCheckDTO::getResourceId)
                .collect(Collectors.toList());
        List<Long> failedIds = rePublishResource(ids, kind);
        return DataCorrectResultDTO.ofTotalAndFailed(ids, failedIds);
    }


    private List<Long> rePublishResource(List<Long> ids, String kind){
        if (DESTINATION_RULE.equals(kind) || SERVICE_ENTRY.equals(kind)){
            return gPortalHttpUtil.rePublishService(ids);
        }else if (VIRTUAL_SERVICE.equals(kind)){
            return gPortalHttpUtil.rePublishRouteRule(ids);
        }else {
            return new ArrayList<>();
        }
    }

    private List<ResourceCheckDTO> checkResourceInfo(List<ResourceDTO> dbResource, List<ResourceDTO> crResource, Function<ResourceDTO, String> matchCondition){
        if (dbResource == null) dbResource = new ArrayList<>();
        if (crResource == null) crResource = new ArrayList<>();
        Map<String, ResourceDTO> dbResourceMap = dbResource.stream().filter(o -> StringUtils.isNotBlank(o.getResourceName())).collect(
                Collectors.groupingBy(ResourceDTO::getResourceName, Collectors.collectingAndThen(Collectors.toList(), value->value.get(0))));
        Map<String, ResourceDTO> crResourceMap = crResource.stream().filter(o -> StringUtils.isNotBlank(o.getResourceName())).collect(
                Collectors.groupingBy(ResourceDTO::getResourceName, Collectors.collectingAndThen(Collectors.toList(), value->value.get(0))));

        Set<String> allResourceName = getAllResourceName(dbResource, crResource);
        List<ResourceCheckDTO> resourceCheckDTOS = new ArrayList<>();
        for (String resourceName : allResourceName) {
            ResourceDTO dbResourceDTO = dbResourceMap.get(resourceName);
            ResourceDTO crResourceDTO = crResourceMap.get(resourceName);
            String dbData = dbResourceDTO == null ? null : matchCondition.apply(dbResourceDTO);
            String crData = crResourceDTO == null ? null : matchCondition.apply(crResourceDTO);
            if (!StringUtils.equals(dbData, crData)){
                Long resourceId = dbResourceDTO == null ? null : dbResourceDTO.getResourceId();
                resourceCheckDTOS.add(ResourceCheckDTO.of(resourceId, resourceName, dbData, crData));
            }
        }
        return resourceCheckDTOS;
    }

    private List<ResourceCheckDTO> checkServiceEntry(List<ResourceDTO> dbResource){
        List<HasMetadata> istioResources = getCustomResource(SERVICE_ENTRY);
        List<ResourceDTO> seResource = istioResources.stream().map(this::convertResourcesByServiceEntry).collect(Collectors.toList());
        List<ResourceDTO> staticDbResource = dbResource.stream().filter(o -> o.getResourceName().startsWith("static")).collect(Collectors.toList());
        return checkResourceInfo(staticDbResource, seResource, ResourceDTO::getBackendService);
    }


    public List<ResourceDTO> getResourceDTO(String kind){
        Predicate<HasMetadata> predicate = o -> true;
//        //过滤路由插件
//        if(GATEWAY_PLUGIN.equals(kind)){
//            predicate = o -> !o.getMetadata().getName().endsWith(gwClusterName);
//        }
        return getCustomResource(kind).stream().filter(predicate).map(this::convert2ResourceDTO).filter(Objects::nonNull).collect(Collectors.toList());
    }


    public List<HasMetadata> getCustomResource(String kind){
        //开启informer缓存，优先从cache中获取
        if (multiClusterK8sClient.watchResource()){
            return k8sResourceCache.getResource(kind);
        }
        return k8sConfigStore.get(kind, globalConfig.getResourceNamespace());
    }

    private Set<String> getAllResourceName(List<ResourceDTO> dbResource, List<ResourceDTO> drResource){
        Set<String> resourceName = new HashSet<>();
        List<String> dbResourceName = dbResource.stream().map(ResourceDTO::getResourceName).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        List<String> drResourceName = drResource.stream().map(ResourceDTO::getResourceName).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dbResourceName)){
            resourceName.addAll(dbResourceName);
        }
        if (CollectionUtils.isNotEmpty(drResourceName)){
            resourceName.addAll(drResourceName);
        }
        return resourceName;
    }


    private ResourceDTO convert2ResourceDTO(HasMetadata hasMetadata){
        ResourceDTO resourceDTO = new ResourceDTO();
        resourceDTO.setResourceName(hasMetadata.getMetadata().getName());
        Map<String, String> labels = hasMetadata.getMetadata().getLabels();
        if (labels == null){
            return null;
        }
        String versionStr = labels.get(DATA_VERSION);
        if (NumberUtils.isCreatable(versionStr)){
            resourceDTO.setResourceVersion(versionStr);
        }
        return resourceDTO;
    }


    private ResourceDTO convertResourcesByServiceEntry(HasMetadata hasMetadata){
        IstioResource ir = (IstioResource) hasMetadata;
        ServiceEntrySpec spec = (ServiceEntrySpec) ir.getSpec();
        List<Endpoint> endpoints = spec.getEndpoints();
        ResourceDTO resourceDTO = new ResourceDTO();
        String name = hasMetadata.getMetadata().getName() + "-" + gwClusterName;
        resourceDTO.setResourceName(name);
        List<String> addressList = new ArrayList<>();
        for (Endpoint endpoint : endpoints) {
            Map<String, String> labels = endpoint.getLabels();
            if (StringUtils.equals(labels.get(GW_CLUSTER), gwClusterName)){
                String address = endpoint.getAddress();
                Map<String, Integer> portMap = endpoint.getPorts();
                if (MapUtils.isNotEmpty(portMap) && portMap.containsKey("http")){
                    Integer port = portMap.get("http");
                    address = address + ":" + port;
                }
                addressList.add(address);
            }
        }
        String backendService = StringUtils.join(addressList, ",");
        resourceDTO.addResourceInfo(ResourceDTO.BackendService, backendService);
        return resourceDTO;
    }
}