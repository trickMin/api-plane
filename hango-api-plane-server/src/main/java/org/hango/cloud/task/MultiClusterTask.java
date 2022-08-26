package org.hango.cloud.task;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hango.cloud.cache.K8sResourceCache;
import org.hango.cloud.core.k8s.MultiClusterK8sClient;
import org.hango.cloud.meta.dto.ApiPlaneResult;
import org.hango.cloud.meta.dto.DataCorrectResultDTO;
import org.hango.cloud.meta.dto.ResourceCheckDTO;
import org.hango.cloud.service.MultiClusterService;
import org.hango.cloud.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hango.cloud.service.impl.MultiClusterServiceImpl.*;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:18
 **/
@Component
public class MultiClusterTask {

    private static final Logger logger = LoggerFactory.getLogger(MultiClusterTask.class);

    @Autowired
    private MultiClusterService multiClusterService;

    @Autowired
    private MultiClusterK8sClient multiClusterK8sClient;

    @Autowired
    private K8sResourceCache k8sResourceCache;


    @Value("${k8s.startCheckTask}")
    private boolean startCheckTask;
    @Scheduled(cron = "${k8s.checkTaskCron}")
    public void dataModifyTask() {
        if (!startCheckTask){
            return;
        }
        logger.info("============================");
        logger.info("多集群校验 | 开始执行校验任务");
        ApiPlaneResult<Map<String, List<ResourceCheckDTO>>> result = multiClusterService.dataCheck();
        if(result.isFailed()){
            logger.error("多集群校验 | 校验任务失败:{}", result.getErrorMsg());
            return;
        }
        logger.info("多集群校验 | 校验执行成功");
        //处理校验结果
        boolean needCorrect = handleCheckResult(result.getData());
        if (!needCorrect){
            return;
        }
        logger.info("多集群补偿 | 开始执行补偿任务（不删除资源）");
        Map<String, DataCorrectResultDTO> correctResult = multiClusterService.dataCorrection(result.getData());
        //处理补偿结果
        handleCorrectResult(correctResult);
    }

    private void refreshCache(Map<String, List<ResourceCheckDTO>> map){
        if (map == null){
            return;
        }
        for (Map.Entry<String, List<ResourceCheckDTO>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<ResourceCheckDTO> value = entry.getValue();
            if (CollectionUtils.isNotEmpty(value)){
                List<String> resourceNames = value.stream().map(ResourceCheckDTO::getResourceName).collect(Collectors.toList());
                logger.info("start refresh cache, kind:{}, names:{}", key, CommonUtil.toJSONString(resourceNames));
                k8sResourceCache.refresh(key, resourceNames);
            }

        }
    }

    private boolean handleCheckResult(Map<String, List<ResourceCheckDTO>> checkResult){
        if (MapUtils.isEmpty(checkResult)){
            logger.info("多集群校验 | 校验结果：未发现数据不一致");
            return false;
        }
        boolean drNeedCorrect = false;
        boolean vsNeedCorrect = false;
        boolean seNeedCorrect = false;
        if (checkResult.containsKey(DESTINATION_RULE)){
            drNeedCorrect = doHandleCheckResult(checkResult.get(DESTINATION_RULE), DESTINATION_RULE);
        }
        if (checkResult.containsKey(VIRTUAL_SERVICE)){
            vsNeedCorrect = doHandleCheckResult(checkResult.get(VIRTUAL_SERVICE), VIRTUAL_SERVICE);
        }
        if (checkResult.containsKey(SERVICE_ENTRY)){
            seNeedCorrect = doHandleCheckResult(checkResult.get(SERVICE_ENTRY), SERVICE_ENTRY);
        }

        return drNeedCorrect || vsNeedCorrect || seNeedCorrect;
    }

    private boolean doHandleCheckResult(List<ResourceCheckDTO> result, String kind){
        if (CollectionUtils.isEmpty(result)){
            return false;
        }
        List<String> needDeleteCustomResource = new ArrayList<>();
        List<ResourceCheckDTO> needUpdateCustomResource = new ArrayList<>();
        for (ResourceCheckDTO resourceCheckDTO : result) {
            if (StringUtils.isBlank(resourceCheckDTO.getDbResourceInfo())){
                needDeleteCustomResource.add(resourceCheckDTO.getResourceName());
            }else {
                needUpdateCustomResource.add(resourceCheckDTO);
            }
        }
        logger.error("多集群校验 | {}校验结果", kind);
        if (CollectionUtils.isNotEmpty(needDeleteCustomResource)){
            logger.error("多集群校验 | 存在需要删除的资源：");
            logger.error("{}", CommonUtil.toJSONString(needDeleteCustomResource));
        }
        if (CollectionUtils.isNotEmpty(needUpdateCustomResource)){
            logger.error("多集群校验 | 存在需要更新的资源：");
            logger.error("{}", CommonUtil.toJSONString(needUpdateCustomResource));
        }
        return CollectionUtils.isNotEmpty(needUpdateCustomResource);
    }

    private void handleCorrectResult(Map<String, DataCorrectResultDTO>correctResult){
        if (MapUtils.isEmpty(correctResult)){
            return;
        }
        if (correctResult.containsKey(DESTINATION_RULE)){
            doHandleCorrectResult(correctResult.get(DESTINATION_RULE), DESTINATION_RULE);
        }
        if (correctResult.containsKey(VIRTUAL_SERVICE)){
            doHandleCorrectResult(correctResult.get(VIRTUAL_SERVICE), VIRTUAL_SERVICE);
        }
        if (correctResult.containsKey(SERVICE_ENTRY)){
            doHandleCorrectResult(correctResult.get(SERVICE_ENTRY), SERVICE_ENTRY);
        }
//        if (correctResult.containsKey(GATEWAY_PLUGIN)){
//            doHandleCorrectResult(correctResult.get(GATEWAY_PLUGIN), GATEWAY_PLUGIN);
//        }
    }
    private void doHandleCorrectResult(DataCorrectResultDTO result, String kind){
        if (result == null){
            return;
        }
        List<Long> successList = result.getSuccessList() == null ? new ArrayList<>() : result.getSuccessList();
        List<Long> failedList = result.getFaildList() == null ? new ArrayList<>() : result.getFaildList();
        logger.error("多集群补偿 | {}补偿结果", kind);
        logger.error("重新发布数：{}, 发布成功：{}， 发布失败:{}", result.getTotalCount(), successList, failedList);

    }
}
