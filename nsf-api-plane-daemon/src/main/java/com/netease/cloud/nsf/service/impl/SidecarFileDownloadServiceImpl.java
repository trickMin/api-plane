package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.config.DaemonSetConfig;
import com.netease.cloud.nsf.config.NosConfig;
import com.netease.cloud.nsf.meta.SidecarVersionInfoDto;
import com.netease.cloud.nsf.service.SidecarFileDownloadService;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.rest.RestTemplateClient;
import com.netease.cloud.services.nos.NosClient;
import com.netease.cloud.services.nos.model.GetObjectRequest;
import com.netease.cloud.services.nos.model.ObjectMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class SidecarFileDownloadServiceImpl implements SidecarFileDownloadService {

    private static final Logger log = LoggerFactory.getLogger(SidecarFileDownloadServiceImpl.class);


    @Autowired
    private DaemonSetConfig daemonSetConfig;

    @Autowired
    private RestTemplateClient restTemplateClient;

    @Autowired
    private NosClient nosClient;

    @Autowired
    private NosConfig nosConfig;

    private final String SIDECAR_URL = "/api/servicemesh?Version=2018-05-31&Action=";

    @Override
    public void downloadSidecar(String sidecarVersion) {
        String fileName = getFilePath(sidecarVersion);
        File sidecarFile = new File(fileName);
        if (!sidecarFile.exists()) {
            // 从NOS中下载对应的envoy文件
            downloadFromNOS(sidecarVersion, fileName);
        }
        if (sidecarFile == null) {
            throw new RuntimeException("downLoad sidecar file from nsf-meta error");
        }
    }

    @Override
    public void deleteSidecar(String sidecarVersion) {
        String fileName = getFilePath(sidecarVersion);
        File sidecarFile = new File(fileName);
        if (sidecarFile.exists()) {
            sidecarFile.delete();
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void synchronizeSidecarFile() {
        List<SidecarVersionInfoDto> sidecarList = getSidecarList();
        if (!CollectionUtils.isEmpty(sidecarList)) {
            for (SidecarVersionInfoDto sidecarVersionInfoDto : sidecarList) {
                downloadSidecar(sidecarVersionInfoDto.getFileName());
            }
        }

    }

    public List<SidecarVersionInfoDto> getSidecarList() {
        if (StringUtils.isEmpty(daemonSetConfig.getNsfMetaUrl())) {
            log.error("get sidecar list fail , nsf-meta url can't be null");
            return new ArrayList<>();
        }
        String url = daemonSetConfig.getNsfMetaUrl() + SIDECAR_URL + "GetSidecarInfoList";
        String response = restTemplateClient.getForValue(url, new HashMap<>(), Const.GET_METHOD, String.class);
        List<SidecarVersionInfoDto> result = restTemplateClient.getArrayFromResponse("Result", SidecarVersionInfoDto.class, response);
        if (CollectionUtils.isEmpty(result)) {
            return new ArrayList<>();
        }
        return result;
    }

    private String getFilePath(String sidecarVersion) {

        String filePath = daemonSetConfig.getSidecarFilePath();
        if (filePath.endsWith("/")) {
            filePath += sidecarVersion;
        } else {
            filePath = filePath + "/" + sidecarVersion;
        }

        return filePath;
    }

    private void downloadFromNOS(String sidecarVersion, String filePath) {
        String nosFilePath = nosConfig.getNosFilePath()
                + "/"
                + sidecarVersion;
        GetObjectRequest getObjectRequest = new GetObjectRequest(nosConfig.getNosBucketName(), nosFilePath);
        ObjectMetadata objectMetadata = nosClient.getObject(getObjectRequest, new File(filePath));
        log.info("download file from nos path {} to local path {}",nosFilePath,filePath);
    }


}
