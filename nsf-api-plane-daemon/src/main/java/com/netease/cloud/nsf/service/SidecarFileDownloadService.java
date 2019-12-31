package com.netease.cloud.nsf.service;



/**
 * @author zhangzihao
 */
public interface SidecarFileDownloadService {


    public void downloadSidecar(String sidecarVersion);


    void deleteSidecar(String sidecarVersion);
}
