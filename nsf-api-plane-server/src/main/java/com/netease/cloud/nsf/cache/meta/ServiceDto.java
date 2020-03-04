package com.netease.cloud.nsf.cache.meta;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceDto<T extends HasMetadata> extends K8sResourceDTO {

    private Map<String, String> labels;

    private Map<String, String> selectLabels;

    private String appName;

    private String serviceType;

    private List<String> serviceAddress;


    public ServiceDto(T obj, String clusterId) {
        super(obj,clusterId);
        Service service = (Service)obj;
        this.labels = service.getMetadata().getLabels();
        this.selectLabels = service.getSpec().getSelector();
        this.serviceType = service.getSpec().getType();
        String address = service.getMetadata().getName() + "." + this.getNamespace();
        List<String> serviceAddressList = new ArrayList<>();

        List<ServicePort> ports = service.getSpec().getPorts();
        if (CollectionUtils.isEmpty(ports)){
            serviceAddressList.add(address);
        }else {
            ports.forEach(p->{
                serviceAddressList.add(address + ":"+p.getPort());
            });
        }
        this.serviceAddress = serviceAddressList;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public List<String> getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(List<String> serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getSelectLabels() {
        return selectLabels;
    }

    public void setSelectLabels(Map<String, String> selectLabels) {
        this.selectLabels = selectLabels;
    }
}
