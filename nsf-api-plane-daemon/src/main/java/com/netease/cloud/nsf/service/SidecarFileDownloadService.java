package com.netease.cloud.nsf.service;



/**
 * @author zhangzihao
 */
public interface SidecarFileDownloadService {


    public void downloadSidecar(String sidecarVersion) throws InterruptedException;


    void deleteSidecar(String sidecarVersion);
}
