package com.netease.cloud.nsf.configuration;

import com.netease.cloud.nsf.util.Const;

/**
 * @author zhangzihao
 */
public class ApiPlaneConfig {

    private String nsfMetaUrl;

    private String startInformer = Const.OPTION_TRUE;

    public String getStartInformer() {
        return startInformer;
    }

    public void setStartInformer(String startInformer) {
        this.startInformer = startInformer;
    }

    public String getNsfMetaUrl() {
        return nsfMetaUrl;
    }

    public void setNsfMetaUrl(String nsfMetaUrl) {
        this.nsfMetaUrl = nsfMetaUrl;
    }
}
