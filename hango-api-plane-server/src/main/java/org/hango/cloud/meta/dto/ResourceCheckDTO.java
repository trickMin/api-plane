package org.hango.cloud.meta.dto;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/26 15:03
 **/
public class ResourceCheckDTO {
    private Long resourceId;

    private String resourceName;

    private String dbResourceInfo;

    private String crResourceInfo;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getDbResourceInfo() {
        return dbResourceInfo;
    }

    public void setDbResourceInfo(String dbResourceInfo) {
        this.dbResourceInfo = dbResourceInfo;
    }

    public String getCrResourceInfo() {
        return crResourceInfo;
    }

    public void setCrResourceInfo(String crResourceInfo) {
        this.crResourceInfo = crResourceInfo;
    }

    public ResourceCheckDTO(Long resourceId, String resourceName, String dbResourceInfo, String crResourceInfo) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.dbResourceInfo = dbResourceInfo;
        this.crResourceInfo = crResourceInfo;
    }

    public ResourceCheckDTO() {
    }

    public static ResourceCheckDTO of(Long resourceId, String resourceName, String dbResourceInfo, String crResourceInfo){
        return new ResourceCheckDTO(resourceId, resourceName, dbResourceInfo, crResourceInfo);
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}
