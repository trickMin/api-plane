package com.netease.cloud.nsf.meta;



/**
 * @Author chenjiahan | chenjiahan@corp.netease.com | 2019/9/20
 **/
public class Service {

    private String code;

    /**
     * 对应后端服务
     */
    private String backendService;

    /**
     * 类型
     */
    private String type;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 所属网关
     */
    private String gateway;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBackendService() {
        return backendService;
    }

    public void setBackendService(String backendService) {
        this.backendService = backendService;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
}
