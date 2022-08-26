package org.hango.cloud.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hango.cloud.meta.dto.ApiPlaneResult;
import org.hango.cloud.meta.dto.ResourceDTO;
import org.hango.cloud.util.errorcode.ErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:06
 **/
@Component
public class GPortalHttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(GPortalHttpUtil.class);

    @Value(value = "${gportal.url}")
    private String gportalAddress;

    @Value(value = "${gportal.gwId}")
    private Long gwId;


    public static final String GATEWAY_PREFIX = "gdashboard/envoy";

    public static final String HEADER_X_AUTH_PROJECT_ID = "x-auth-projectId";

    public static final String HEADER_X_AUTH_TENANT_ID = "x-auth-tenantId";

    public static final String HEADER_X_163_ACCEPT_LANGUAGE = "X_163_AcceptLanguage";

    public static final String SUCCESS = "Success";

    public ApiPlaneResult<Map<String, List<ResourceDTO>>> getResourceInfoFromDB(){
        String url = gportalAddress + GATEWAY_PREFIX;
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("Action", "GetResourceInfo");
        queryMap.put("Version", "2019-09-01");
        queryMap.put("GwId", String.valueOf(gwId));
        Map<String, String> headerMap = getDefaultHeader();
        ApiPlaneResult<String> result = OKHttpUtil.get(url, headerMap, queryMap);
        if (result.isFailed()){
            logger.error("查询资源信息失败:{}", result.getErrorMsg());
            return ApiPlaneResult.ofFailed(ErrorCodeEnum.HttpRemoteError, result.getErrorMsg());
        }
        String data = result.getData();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode resources;
        try {
            resources = objectMapper.readTree(data).get("Result");
            if (resources == null){
                return ApiPlaneResult.ofSuccess(new HashMap<>());
            }
            Map<String, List<ResourceDTO>> resourceMap = objectMapper.convertValue(resources, new TypeReference<Map<String, List<ResourceDTO>>>(){});
            return ApiPlaneResult.ofSuccess(resourceMap);
        } catch (JsonProcessingException e) {
            logger.error("解析响应体异常, response:{}", data, e);
            return ApiPlaneResult.ofFailed(ErrorCodeEnum.HttpRemoteError);
        }
    }

    /**
     * 重新发布路由
     * @return 发布失败的路由id
     */
    public List<Long> rePublishRouteRule(List<Long> routeRuleIds){
        String url = gportalAddress + GATEWAY_PREFIX;
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("Action", "RePublishRouteRule");
        queryMap.put("Version", "2019-09-01");
        Map<String, Object> body = new HashMap<>();
        body.put("RePublishAllRouteRule", false);
        body.put("GwId", gwId);
        body.put("RouteRuleIdList", routeRuleIds);
        Map<String, String> headerMap = getDefaultHeader();
        ApiPlaneResult<List<Long>> result = publishResource(url, headerMap, queryMap, body);
        if (result.isFailed()){
            return routeRuleIds;
        }
        return result.getData();
    }


    /**
     * 重新发布服务
     * @return 发布失败的服务id
     */
    public List<Long> rePublishService(List<Long> serviceIds){
        String url = gportalAddress + GATEWAY_PREFIX;
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("Action", "RePublishService");
        queryMap.put("Version", "2019-09-01");
        Map<String, Object> body = new HashMap<>();
        body.put("RePublishAllService", false);
        body.put("GwId", gwId);
        body.put("ServiceIdList", serviceIds);
        Map<String, String> headerMap = getDefaultHeader();
        ApiPlaneResult<List<Long>> result = publishResource(url, headerMap, queryMap, body);
        if (result.isFailed()){
            return serviceIds;
        }
        return result.getData();
    }

//    /**
//     * 重新发布全局插件
//     * @return 发布失败的bindingobjectid
//     */
//    public List<Long> rePublishGlobalPlugin(List<Long> bindingObjectIdList){
//        String url = gportalAddress + GATEWAY_PREFIX;
//        Map<String, String> queryMap = new HashMap<>();
//        queryMap.put("Action", "RePublishPlugin");
//        queryMap.put("Version", "2019-09-01");
//        Map<String, Object> body = new HashMap<>();
//        body.put("GwId", gwId);
//        body.put("BindingObjectIdList", bindingObjectIdList);
//        body.put("BindingObjectType", "global");
//        Map<String, String> headerMap = getDefaultHeader();
//        ApiPlaneResult<List<Long>> result = publishResource(url, headerMap, queryMap, body);
//        if (result.isFailed()){
//            return bindingObjectIdList;
//        }
//        return result.getData();
//    }


    private ApiPlaneResult<List<Long>> publishResource(@Nonnull String url, Map<String, String> headerParameter, Map<String, String> queryParameter, Map<String, Object> body){
        ObjectMapper objectMapper = new ObjectMapper();
        String bodyStr;
        try {
            bodyStr = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            logger.error("body格式错误", e);
            return ApiPlaneResult.ofInvaildParam("convert body error");
        }
        ApiPlaneResult<String> result = OKHttpUtil.post(url, headerParameter, queryParameter, bodyStr);
        String data = result.getData();
        if (result.isFailed()){
            return ApiPlaneResult.ofFailed(result.getErrorCode());
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(data).get("FailedIdList");
            List<Long> failedIds = new ArrayList<>();
            if (jsonNode != null){
                failedIds = objectMapper.convertValue(jsonNode, new TypeReference<List<Long>>() {});
            }
            return ApiPlaneResult.ofSuccess(failedIds);
        } catch (JsonProcessingException e) {
            logger.error("解析响应体异常, query:{}, response:{}", CommonUtil.toJSONString(queryParameter), data,e);
            return ApiPlaneResult.ofFailed(ErrorCodeEnum.InvalidParameters);
        }
    }


    private Map<String, String> getDefaultHeader(){
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(HEADER_X_163_ACCEPT_LANGUAGE, "zh");
        return headerMap;
    }

}
