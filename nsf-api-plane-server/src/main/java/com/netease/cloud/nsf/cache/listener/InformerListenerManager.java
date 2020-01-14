package com.netease.cloud.nsf.cache.listener;


import com.netease.cloud.nsf.cache.ResourceUpdateEvent;
import com.netease.cloud.nsf.cache.ResourceUpdatedListener;
import com.netease.cloud.nsf.configuration.ApiPlaneConfig;
import com.netease.cloud.nsf.util.Const;
import com.netease.cloud.nsf.util.RestTemplateClient;
import com.netease.cloud.nsf.util.exception.ApiPlaneException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangzihao
 */
@Component
public class InformerListenerManager {

    @Autowired
    private RestTemplateClient restTemplateClient;

    private static final Logger log = LoggerFactory.getLogger(InformerListenerManager.class);

    @Autowired
    ApiPlaneConfig config;

    private ServiceAutoImportListener serviceAutoImportListener;

    @PostConstruct
    public void init(){
        serviceAutoImportListener = new ServiceAutoImportListener();
    }


    public class ServiceAutoImportListener implements ResourceUpdatedListener {

        private static final String CREATE_SERVICE_URL = "/api/metadata?Version=2018-11-1&Action=CreateServiceForService";

        @Override
        public void notify(ResourceUpdateEvent e) {

            HasMetadata service = e.getResourceObject();
            Map<String, String> serviceLabels = e.getResourceObject().getMetadata().getLabels();
            String projectCode = serviceLabels.get(Const.LABEL_NSF_PROJECT_ID);
            String envName = serviceLabels.get(Const.LABEL_NSF_ENV);
            String namespace = service.getMetadata().getNamespace();
            String name = service.getMetadata().getName();
            if (StringUtils.isBlank(name) || StringUtils.isBlank(namespace) || StringUtils.isBlank(projectCode)){
                log.warn("Service ");
            }
            
            if (StringUtils.isBlank(envName)) {
                envName = namespace;
            }
            String serviceName = name + "." + namespace;
            importService(serviceName,projectCode,envName);

        }

        private void importService(String serviceName, String projectCode, String envName) {
            Map<String, String> requestParam = new HashMap<>(4);
            requestParam.put("serviceName", serviceName);
            requestParam.put("projectCode", projectCode);
            requestParam.put("envName", envName);
            requestParam.put("desc", "auto import");
            String url = restTemplateClient.buildRequestUrlWithParameter(config.getNsfMetaUrl()
                    + CREATE_SERVICE_URL, requestParam);
            try {
                restTemplateClient.getForValue(url
                        , requestParam
                        , Const.GET_METHOD
                        , String.class);
            } catch (ApiPlaneException e) {
                log.warn("create version error {}", e.getMessage());
                return;
            }
            log.info("import service [{}] in projectId [{}] at env [{}]", serviceName, projectCode, envName);
        }
    }
}
