package org.hango.cloud.meta.dto;

import java.util.List;
import java.util.Map;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/30 16:55
 **/
public class ResourceCheckDTO {

    Map<String, List<ResourceDTO>> resource;

    String Gateway;

    public Map<String, List<ResourceDTO>> getResource() {
        return resource;
    }

    public void setResource(Map<String, List<ResourceDTO>> resource) {
        this.resource = resource;
    }

    public String getGateway() {
        return Gateway;
    }

    public void setGateway(String gateway) {
        Gateway = gateway;
    }
}
