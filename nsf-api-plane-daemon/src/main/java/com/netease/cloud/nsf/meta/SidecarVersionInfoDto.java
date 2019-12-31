package com.netease.cloud.nsf.meta;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author zhangzihao
 */
public class SidecarVersionInfoDto {

     @JsonProperty(value = "Id")
    private long id;
     @JsonProperty(value = "SidecarVersion")
    private String version;
     @JsonProperty(value = "FileName")
    private String fileName;
     @JsonProperty(value = "UploadTime")
    private long uploadTime;
     @JsonProperty(value = "IsDefault")
    private int isDefault;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public int getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }
}
