package com.netease.cloud.nsf.mcp;

import istio.mcp.v1alpha1.Mcp;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/13
 **/
public class WatchResponse {
    private String snapshotVersion;
    private Mcp.Resources resource;

    public WatchResponse(String snapshotVersion, Mcp.Resources resource) {
        this.snapshotVersion = snapshotVersion;
        this.resource = resource;
    }

    public String getSnapshotVersion() {
        return snapshotVersion;
    }

    public void setSnapshotVersion(String snapshotVersion) {
        this.snapshotVersion = snapshotVersion;
    }

    public Mcp.Resources getResource() {
        return resource;
    }

    public void setResource(Mcp.Resources resource) {
        this.resource = resource;
    }
}
