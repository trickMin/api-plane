package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.editor.ResourceType;
import com.netease.cloud.nsf.core.gateway.service.ConfigStore;
import com.netease.cloud.nsf.core.k8s.K8sResourceEnum;
import com.netease.cloud.nsf.core.k8s.K8sResourceGenerator;
import com.netease.cloud.nsf.service.ServiceMeshService;
import me.snowdrop.istio.api.IstioResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/11/7
 **/
@Service
public class ServiceMeshServiceImpl implements ServiceMeshService {

    @Autowired
    ConfigStore configStore;

    @Override
    public void updateIstioResource(String json) {

        json = optimize(json);
        configStore.update(json2Resource(json));
    }

    @Override
    public void deleteIstioResource(String json) {

        json = optimize(json);
        configStore.delete(json2Resource(json));
    }

    private String optimize(String json) {
        if (json.startsWith("\"") && json.startsWith("\"")) {
            json = json.substring(1, json.length() - 1);
        }
        return json;
    }

    private IstioResource json2Resource(String json) {
        K8sResourceGenerator gen = K8sResourceGenerator.newInstance(json, ResourceType.JSON);
        K8sResourceEnum resourceEnum = K8sResourceEnum.get(gen.getKind());
        return (IstioResource) gen.object(resourceEnum.mappingType());
    }

}
