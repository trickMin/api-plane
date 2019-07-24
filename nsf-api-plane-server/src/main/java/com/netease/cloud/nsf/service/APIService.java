package com.netease.cloud.nsf.service;

import com.netease.cloud.nsf.meta.APIModel;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/7/17
 **/
public interface APIService {

    void updateAPI(APIModel api);

    void deleteAPI(String service, String name);
}