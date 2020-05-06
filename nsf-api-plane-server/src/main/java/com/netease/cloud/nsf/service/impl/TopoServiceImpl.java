package com.netease.cloud.nsf.service.impl;

import com.netease.cloud.nsf.core.k8s.MultiClusterK8sClient;
import com.netease.cloud.nsf.core.kiali.KialiHttpClient;
import com.netease.cloud.nsf.meta.Graph;
import com.netease.cloud.nsf.service.TopoService;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/12/10
 **/
@Service
public class TopoServiceImpl implements TopoService {

    private static final Logger logger = LoggerFactory.getLogger(TopoServiceImpl.class);

    @Autowired
    private KialiHttpClient kialiHttpClient;

    @Autowired
    private MultiClusterK8sClient multiClusterK8sClient;

    @Override
    public Graph getAppGraph(String namespaces, String duration, String graphType, boolean injectServices) {

        Set<String> existNss = new HashSet<>();

        //从多个集群中找到所有namespace
        multiClusterK8sClient.getAllClients().forEach((k, client) -> {
            NamespaceList list = client.originalK8sClient.namespaces().list();
            List<Namespace> items = list.getItems();
            if (!CollectionUtils.isEmpty(items)) {
                items.stream().forEach(ns -> existNss.add(ns.getMetadata().getName()));
            }
        });

        //跟传入的namespace相比，去掉不存在的namespace
        List<String> safeNss = new ArrayList<>();
        String[] inputNss = namespaces.split(",");
        for (String inputNs : inputNss) {
            if (existNss.contains(inputNs)) {
                safeNss.add(inputNs);
            }
        }
        if (safeNss.isEmpty()) return new Graph();
        //传入安全的namespace,多集群下可能还是有问题，看具体拓扑的数据
        return kialiHttpClient.getGraph(String.join(",", safeNss), graphType, duration, injectServices);
    }
}
