package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.kiali.KialiHttpClient;
import com.netease.cloud.nsf.meta.Graph;
import com.netease.cloud.nsf.service.TopoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/10
 **/
@Service
public class TopoServiceImpl implements TopoService {

    private static final Logger logger = LoggerFactory.getLogger(TopoServiceImpl.class);

    @Autowired
    private KialiHttpClient kialiHttpClient;

    @Override
    public Graph getAppGraph(String namespaces, String duration, String graphType) {

        return kialiHttpClient.getGraph(namespaces, graphType, duration);
    }
}
