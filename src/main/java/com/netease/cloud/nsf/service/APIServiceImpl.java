package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.client.ConfigClient;
import com.netease.cloud.nsf.client.ConfigStore;
import com.netease.cloud.nsf.meta.APIModel;
import com.netease.cloud.nsf.meta.IstioResource;

import java.util.List;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
public class APIServiceImpl implements APIService{

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

}