package org.hango.cloud.service;

import org.hango.cloud.meta.dto.ApiPlaneResult;
import org.hango.cloud.meta.dto.DataCorrectResultDTO;
import org.hango.cloud.meta.dto.ResourceCheckDTO;

import java.util.List;
import java.util.Map;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 14:58
 **/
public interface MultiClusterService {
    ApiPlaneResult<Map<String, List<ResourceCheckDTO>>> dataCheck();

    Map<String, DataCorrectResultDTO> dataCorrection(Map<String, List<ResourceCheckDTO>> param);
}
