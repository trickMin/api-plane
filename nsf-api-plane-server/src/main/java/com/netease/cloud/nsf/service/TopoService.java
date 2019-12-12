package com.netease.cloud.nsf.service;


import com.netease.cloud.nsf.meta.Graph;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/10
 **/
public interface TopoService {

    Graph getAppGraph(String namespaces, String duration, String graphType);

}
