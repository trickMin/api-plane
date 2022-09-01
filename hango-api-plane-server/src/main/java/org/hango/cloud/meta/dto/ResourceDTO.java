package org.hango.cloud.meta.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:03
 **/
public class ResourceDTO {

    @JsonProperty("ResourceId")
    private Long resourceId;

    @JsonProperty("ResourceName")
    private String resourceName;

    @JsonProperty("ResourceVersion")
    private String resourceVersion;


    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }


    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }


    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}
