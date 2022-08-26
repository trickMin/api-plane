package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:03
 **/
public class ResourceDTO {
    public static final String BackendService = "backendService";

    @JsonProperty("ResourceId")
    private Long resourceId;

    @JsonProperty("ResourceName")
    private String resourceName;

    @JsonProperty("ResourceVersion")
    private String resourceVersion;

    @JsonProperty("ResourceInfo")
    private Map<String, String> resourceInfo;

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public Map<String, String> getResourceInfo() {
        return resourceInfo;
    }

    public void setResourceInfo(Map<String, String> resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getBackendService(){
        if (resourceInfo == null){
            return null;
        }
        return resourceInfo.get(BackendService);
    }

    public void addResourceInfo(String key, String value){
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)){
            return;
        }
        if (resourceInfo == null){
            resourceInfo = new HashMap<>();
        }
        resourceInfo.put(key, value);
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}
