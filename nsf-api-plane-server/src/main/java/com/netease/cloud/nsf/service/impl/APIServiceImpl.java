package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.client.ConfigClient;
import com.netease.cloud.nsf.core.store.ConfigStore;
import com.netease.cloud.nsf.meta.APIModel;

import com.netease.cloud.nsf.core.ModelProcessor;
import com.netease.cloud.nsf.service.APIService;
import me.snowdrop.istio.api.IstioResource;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
public class APIServiceImpl implements APIService {

    private ModelProcessor modelProcessor;
    private ConfigClient client;
    private ConfigStore store;

    public APIServiceImpl(ModelProcessor processor, ConfigClient client, ConfigStore store) {
        this.modelProcessor = processor;
        this.client = client;
        this.store = store;
    }

    public void updateAPI(APIModel api) {

        List<IstioResource> resources = modelProcessor.translate(api);
        for (IstioResource r : resources) {
            IstioResource c = store.get(r);
            if (c != null) {
                client.updateConfig(modelProcessor.merge(c, r));
            }
            client.updateConfig(r);
        }
    }

    @Override
    public void deleteAPI(String service, String name) {

        APIModel api = APIModel.APIModelBuilder.anAPIModel()
                            .withService(service)
                            .withService(name)
                            .build();


    }




}