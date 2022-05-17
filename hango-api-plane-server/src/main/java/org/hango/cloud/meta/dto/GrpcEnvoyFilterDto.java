package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author xin li
 * @date 2022/5/20 11:16
 */
public class GrpcEnvoyFilterDto {
    /**
     * 网关
     */
    @JsonProperty(value = "GwCluster")
    @NotNull(message = "GwCluster")
    private String gwCluster;

    /**
     * pb文件
     */
    @JsonProperty(value = "ProtoDescriptorBin")
    @NotNull(message = "ProtoDescriptorBin")
    private String protoDescriptorBin;

    /**
     * 要支持协议转换的services
     */
    @JsonProperty(value = "Services")
    @NotNull(message = "Services")
    private List<String> services;

    public String getGwCluster() {
        return gwCluster;
    }

    public void setGwCluster(String gwCluster) {
        this.gwCluster = gwCluster;
    }

    public String getProtoDescriptorBin() {
        return protoDescriptorBin;
    }

    public void setProtoDescriptorBin(String protoDescriptorBin) {
        this.protoDescriptorBin = protoDescriptorBin;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }
}
